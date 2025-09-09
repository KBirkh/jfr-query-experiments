package me.bechberger.jfr.wrap;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import jdk.jfr.consumer.*;

/*
 * Represents a row of data
 * A datapoint can be gotten via the column name
 * which is used as the key for the hashmap
 * A linked hashmap is used to preserve order and output
 * the rows in the same order as the columns are specified in
 * in the table
 */
public class EvalRow {
    LinkedHashMap<String, Object> fields;
    LinkedHashMap<String, Integer> fieldSize;

    public EvalRow(LinkedHashMap<String, Object> fields) {
        this.fields = fields;
        for(Map.Entry<String, Object> entry : fields.entrySet()) {
            if(fieldSize == null) {
                fieldSize = new LinkedHashMap<>();
            }
            String key = entry.getKey();
            Object value = entry.getValue();
            int length = value != null ? value.toString().length() : 4; // "null" has length 4
            fieldSize.put(key, length);
        }
    }

    public EvalRow() {
        this.fields = new LinkedHashMap<>();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            Object value = entry.getValue();
            if(value instanceof RecordedThread) {
                value = ((RecordedThread) value).getOSName(); 
            } else if(value instanceof Duration) {
                long durationNanos = ((Duration) value).toNanos(); // Convert Duration to nanoseconds
                if(durationNanos < 1000) {
                    value = durationNanos + " ns"; // Keep as nanoseconds
                } else if(durationNanos < 1_000_000) {
                    value = durationNanos / 1_000F + " μs"; // Convert Duration to microseconds
                } else {
                    value = (durationNanos / 1_000_000F) + " ms"; // Convert Duration to milliseconds
                }
            } else if (value instanceof RecordedEvent) {
                value = ((RecordedEvent) value).getEventType().getName(); // Use event type name for RecordedEvent
            } else if (value instanceof RecordedObject) {
                value = ((RecordedObject) value).getClass().getSimpleName(); // Use class name for RecordedObject
            }
             else if (value == null) {
                value = "null";
            } else {
                value = value.toString(); // Fallback for other types
            }
            sb.append(value);
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
    
    public EvalRow addField(String name, Object value) {
        if (fields == null) {
            fields = new LinkedHashMap<>();
        }
        fields.put(name, value);
        if(fieldSize == null) {
            fieldSize = new LinkedHashMap<>();
        }
        int length = value != null ? value.toString().length() : 4; // "null" has length 4
        fieldSize.put(name, length);
        return this;
    }

    public void addFieldFirst(String name, Object value) {
        if (fields == null) {
            fields = new LinkedHashMap<>();
        }
        LinkedHashMap<String, Object> newFields = new LinkedHashMap<>();
        newFields.put(name, value);
        newFields.putAll(fields);
        fields = newFields;
        if(fieldSize == null) {
            fieldSize = new LinkedHashMap<>();
        }
        int length = value != null ? value.toString().length() : 4; // "null" has length 4
        LinkedHashMap<String, Integer> newFieldSize = new LinkedHashMap<>();
        newFieldSize.put(name, length);
        newFieldSize.putAll(fieldSize);
        fieldSize = newFieldSize;
    }

    public LinkedHashMap<String, Object> getFields() {
        return fields;
    }

    public Integer getFieldSize(String name) {
        if(fieldSize == null) {
            return null;
        }
        return fieldSize.get(name);
    }
}
