package me.bechberger.jfr.wrap;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedObject;
import jdk.jfr.consumer.RecordedThread;
import me.bechberger.jfr.wrap.nodes.AstNode;

/*
 * The class on which most of the processing is done
 * Contains lists of both columns and rows
 * Keeps a reference to the query root which
 * the table is a result of
 * Keeps record when the table is grouped to check
 * if all original columns are available for projection
 */
public class EvalTable {
    public List<Column> columns;
    public List<EvalRow> rows;
    private AstNode root;
    private boolean isGrouped = false;

    public EvalTable(List<Column> columns, List<EvalRow> rows) {
        this.columns = columns;
        this.rows = rows;
    }

    public void setQuery(AstNode root) {
        this.root = root;
    }

    public AstNode getRoot() {
        return root;
    }

    public void setAlias(String alias) {
        for (Column column : columns) {
            column.setAlias(alias);
        }
    }

    public void addRow(EvalRow row) {
        if (rows == null) {
            rows = new java.util.ArrayList<>();
        }
        rows.add(row);
    }

    /*
     * When adding a new column (e.g. for gc correlation or for aggregates)
     * a column also has to be added to each row
     */
    public void addColumn(Column column) {
        if (columns == null) {
            columns = new java.util.ArrayList<>();
        }
        if(!columns.contains(column)) {
            columns.add(column);
            for(EvalRow row : rows) {
                row.addField(column.getFullName(), null); // Initialize with null for existing rows
            }
        }
    }

    public void addColumnFirst(Column column) {
        if (columns == null) {
            columns = new java.util.ArrayList<>();
        }
        if(!columns.contains(column)) {
            columns.add(0, column);
            for(EvalRow row : rows) {
                row.addField(column.getFullName(), null); // Initialize with null for existing rows
            }
        }
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public List<EvalRow> getRows() {
        return rows;
    }

    public boolean isGrouped() {
        return isGrouped;
    }

    public void setGrouped(boolean isGrouped) {
        this.isGrouped = isGrouped;
    }

    public void limit(int count) {
        if (rows != null && rows.size() > count) {
            rows = rows.subList(0, count);
        }
    }

    public String toJson() {
        if (rows == null || rows.isEmpty() || columns == null || columns.isEmpty()) {
            return "{\n  \"header\": {\n    \"columns\": [],\n    \"types\": []\n  },\n  \"data\": []\n}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        
        // Header section with column names and types
        sb.append("  \"header\": {\n");
        sb.append("    \"columns\": [");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("\"").append(escapeJsonString(columns.get(i).getFullName())).append("\"");
        }
        sb.append("],\n");
        
        sb.append("    \"types\": [");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(", ");
            Column col = columns.get(i);
            String type = determineJsonType(col);
            sb.append("\"").append(type).append("\"");
        }
        sb.append("]\n");
        sb.append("  },\n");
        
        // Data section
        sb.append("  \"data\": [\n");
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            if (rowIndex > 0) sb.append(",\n");
            EvalRow row = rows.get(rowIndex);
            sb.append("    {\n");
            
            for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
                if (colIndex > 0) sb.append(",\n");
                String columnName = columns.get(colIndex).getFullName();
                Object value = row.getFields().get(columnName);
                sb.append("      \"").append(escapeJsonString(columnName)).append("\": ");
                sb.append(formatJsonValue(value, 3)); // Base indentation level for top-level objects
            }
            sb.append("\n    }");
        }
        sb.append("\n  ]\n");
        sb.append("}");
        
        return sb.toString();
    }

    private String determineJsonType(Column column) {
        // Sample the first few rows to determine the most appropriate JSON type
        for (EvalRow row : rows) {
            Object value = row.getFields().get(column.getFullName());
            if (value != null) {
                if (value instanceof Number) {
                    return "number";
                } else if (value instanceof Boolean) {
                    return "boolean";
                } else if (value instanceof Duration) {
                    return "duration"; // Duration as nanoseconds
                } else if (value instanceof RecordedThread) {
                    return "object"; // RecordedThread as object
                } else if (value instanceof GCEvent) {
                    return "object"; // GCEvent as object
                } else {
                    return "string"; // Default for other types
                }
            }
        }
        return "string"; // Default if all values are null
    }

    private String formatJsonValue(Object value, int indentLevel) {
        if (value == null) {
            return "null";
        } else if (value instanceof Number) {
            return value.toString();
        } else if (value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof Duration) {
            // Convert Duration to nanoseconds as requested
            return String.valueOf(((Duration) value).toNanos());
        } else if (value instanceof RecordedThread) {
            // Convert RecordedThread to proper JSON object
            return parseRecordedThreadToJson((RecordedThread) value, indentLevel);
        } else if (value instanceof GCEvent) {
            // Convert GCEvent to proper JSON object
            return parseGCEventToJson((GCEvent) value, indentLevel);
        } else if (value instanceof RecordedObject) {
            // Convert RecordedObject to some form of object notation
            return "\"" + escapeJsonString(value.toString()) + "\"";
        } else {
            // Everything else as string
            return "\"" + escapeJsonString(value.toString()) + "\"";
        }
    }

    private String escapeJsonString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    private String getIndent(int level) {
        return " ".repeat(level * 2); // 2 spaces per indent level
    }

    private String parseRecordedThreadToJson(RecordedThread thread, int indentLevel) {
        // Parse RecordedThread toString() output into proper JSON object
        // Example input: "{\n  osName = \"GC Thread#2\"\n  osThreadId = 34051\n  javaName = N/A\n  javaThreadId = 0\n  group = N/A\n  virtual = false\n}"
        
        String fieldIndent = getIndent(indentLevel + 1);
        String closeIndent = getIndent(indentLevel);
        
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        
        // Extract values using the RecordedThread methods directly
        json.append(fieldIndent).append("\"osName\": ");
        String osName = thread.getOSName();
        if (osName != null) {
            json.append("\"").append(escapeJsonString(osName)).append("\"");
        } else {
            json.append("null");
        }
        
        json.append(",\n").append(fieldIndent).append("\"osThreadId\": ").append(thread.getOSThreadId());
        
        json.append(",\n").append(fieldIndent).append("\"javaName\": ");
        String javaName = thread.getJavaName();
        if (javaName != null) {
            json.append("\"").append(escapeJsonString(javaName)).append("\"");
        } else {
            json.append("null");
        }
        
        json.append(",\n").append(fieldIndent).append("\"javaThreadId\": ").append(thread.getJavaThreadId());
        
        json.append(",\n").append(fieldIndent).append("\"group\": ");
        Object group = thread.getThreadGroup();
        if (group != null) {
            json.append("\"").append(escapeJsonString(group.toString())).append("\"");
        } else {
            json.append("null");
        }
        
        json.append(",\n").append(fieldIndent).append("\"virtual\": ").append(thread.isVirtual());
        
        json.append("\n").append(closeIndent).append("}");
        return json.toString();
    }

    private String parseGCEventToJson(GCEvent gcEvent, int indentLevel) {
        // Parse GCEvent into proper JSON object with all available fields
        String fieldIndent = getIndent(indentLevel + 1);
        String closeIndent = getIndent(indentLevel);
        
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        
        // Add all GCEvent fields
        json.append(fieldIndent).append("\"name\": ");
        if (gcEvent.name != null) {
            json.append("\"").append(escapeJsonString(gcEvent.name)).append("\"");
        } else {
            json.append("null");
        }
        
        json.append(",\n").append(fieldIndent).append("\"startTime\": ");
        if (gcEvent.startTime != null) {
            json.append("\"").append(gcEvent.startTime.toString()).append("\"");
        } else {
            json.append("null");
        }
        
        json.append(",\n").append(fieldIndent).append("\"duration\": ");
        if (gcEvent.duration != null) {
            json.append(gcEvent.duration.toNanos());
        } else {
            json.append("null");
        }
        
        json.append(",\n").append(fieldIndent).append("\"eventThread\": ");
        if (gcEvent.eventThread != null) {
            json.append(parseRecordedThreadToJson(gcEvent.eventThread, indentLevel + 1));
        } else {
            json.append("null");
        }
        
        json.append(",\n").append(fieldIndent).append("\"gcId\": ").append(gcEvent.gcId);
        
        json.append(",\n").append(fieldIndent).append("\"cause\": ");
        if (gcEvent.cause != null) {
            json.append("\"").append(escapeJsonString(gcEvent.cause)).append("\"");
        } else {
            json.append("null");
        }
        
        json.append(",\n").append(fieldIndent).append("\"sumOfPauses\": ");
        if (gcEvent.sumOfPauses != null) {
            json.append(gcEvent.sumOfPauses.toNanos());
        } else {
            json.append("null");
        }
        
        json.append(",\n").append(fieldIndent).append("\"longestPause\": ");
        if (gcEvent.longestPause != null) {
            json.append(gcEvent.longestPause.toNanos());
        } else {
            json.append("null");
        }
        
        json.append("\n").append(closeIndent).append("}");
        return json.toString();
    }

    @Override
    public String toString() {
        if (rows == null || rows.isEmpty() || columns == null || columns.isEmpty()) {
            return "Empty table";
        }

        // Calculate column widths based on formatted content
        Map<String, Integer> columnWidths = new LinkedHashMap<>();
        
        // Initialize with column header lengths
        for (Column column : columns) {
            columnWidths.put(column.getFullName(), column.getFullName().length());
        }
        
        // Check each row's formatted values
        for (EvalRow row : rows) {
            for (Column column : columns) {
                String columnName = column.getFullName();
                Object value = row.getFields().get(columnName);
                String formattedValue = formatValue(value);
                int currentWidth = columnWidths.get(columnName);
                columnWidths.put(columnName, Math.max(currentWidth, formattedValue.length()));
            }
        }

        StringBuilder sb = new StringBuilder();
        
        // Build top border
        sb.append("\u250F");
        for (Column column : columns) {
            int width = columnWidths.get(column.getFullName());
            sb.append("\u2501".repeat(width + 2)).append("\u2533");
        }
        sb.replace(sb.length()-1, sb.length(), "\u2513");
        sb.append("\n");
        
        // Build header
        sb.append("\u2503");
        for (Column column : columns) {
            String columnName = column.getFullName();
            int width = columnWidths.get(columnName);
            sb.append(String.format(" %-" + width + "s \u2503", columnName));
        }
        sb.append("\n");
        
        // Build separator
        sb.append("\u2523");
        for (Column column : columns) {
            int width = columnWidths.get(column.getFullName());
            sb.append("\u2501".repeat(width + 2)).append("\u254B");
        }
        sb.replace(sb.length()-1, sb.length(), "\u252B");
        sb.append("\n");
        
        // Build data rows
        for (EvalRow row : rows) {
            sb.append("\u2503");
            for (Column column : columns) {
                String columnName = column.getFullName();
                Object value = row.getFields().get(columnName);
                String formattedValue = formatValue(value);
                int width = columnWidths.get(columnName);
                sb.append(String.format(" %-" + width + "s \u2503", formattedValue));
            }
            sb.append("\n");
        }
        
        // Build bottom border
        sb.append("\u2517");
        for (Column column : columns) {
            int width = columnWidths.get(column.getFullName());
            sb.append("\u2501".repeat(width + 2)).append("\u253B");
        }
        sb.replace(sb.length()-1, sb.length(), "\u251B");
        
        return sb.toString();
    }

    private String formatValue(Object value) {
        if(value instanceof RecordedThread) {
            return ((RecordedThread) value).getOSName(); 
        } else if(value instanceof Duration) {
            long durationNanos = ((Duration) value).toNanos();
            if(durationNanos < 1000) {
                return durationNanos + " ns";
            } else if(durationNanos < 1_000_000) {
                return durationNanos / 1_000F + " μs";
            } else if(durationNanos < 1_000_000_000) {
                return (durationNanos / 1_000_000F) + " ms";
            } else {
                return (durationNanos / 1_000_000_000F) + " s";
            }
        } else if (value instanceof RecordedEvent) {
            return ((RecordedEvent) value).getEventType().getName();
        } else if (value instanceof RecordedObject) {
            return ((RecordedObject) value).getClass().getSimpleName();
        } else if (value == null) {
            return "null";
        } else {
            return value.toString();
        }
    }
}
