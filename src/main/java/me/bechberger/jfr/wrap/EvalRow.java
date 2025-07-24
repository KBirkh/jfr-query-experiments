package me.bechberger.jfr.wrap;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import jdk.jfr.consumer.*;

public class EvalRow {
    LinkedHashMap<String, Object> fields;

    public EvalRow(LinkedHashMap<String, Object> fields) {
        this.fields = fields;
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
                value = ((Duration) value).getNano() + " ns"; // Convert Duration to milliseconds
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
    
    public void addField(String name, Object value) {
        if (fields == null) {
            fields = new LinkedHashMap<>();
        }
        fields.put(name, value);
    }

    public void addFieldFirst(String name, Object value) {
        if (fields == null) {
            fields = new LinkedHashMap<>();
        }
        LinkedHashMap<String, Object> newFields = new LinkedHashMap<>();
        newFields.put(name, value);
        newFields.putAll(fields);
        fields = newFields;
    }

    public LinkedHashMap<String, Object> getFields() {
        return fields;
    }
}
