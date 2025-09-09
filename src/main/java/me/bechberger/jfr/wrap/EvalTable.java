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
