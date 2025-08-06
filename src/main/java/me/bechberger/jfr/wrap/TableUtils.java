package me.bechberger.jfr.wrap;

import java.util.LinkedHashMap;
import java.util.List;
import java.security.cert.CollectionCertStoreParameters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import me.bechberger.jfr.query.Field;
import me.bechberger.jfr.query.Row;
import me.bechberger.jfr.query.Table;

public class TableUtils {

    public static EvalTable toEvalTable(Table table) {
        List<Field> fields = table.getFields();
        List<Row> rows = table.getRows();
        List<Column> columns = fields.stream()
            .map(field -> new Column(field.getName(), field.getType()))
            .collect(Collectors.toCollection(ArrayList::new));
        List<EvalRow> evalRows = rows.stream()
            .map(row -> {
                LinkedHashMap<String, Object> evalRow = new LinkedHashMap<String, Object>();
                for (int i = 0; i < fields.size(); i++) {
                    evalRow.put(columns.get(i).getFullName(), row.getValue(i));
                }
                return new EvalRow(evalRow);
            })
            .collect(Collectors.toCollection(ArrayList::new));
        return new EvalTable(columns, evalRows);
    }

    public static EvalTable toEvalTable(Table table, String alias) {
        List<Field> fields = table.getFields();
        List<Row> rows = table.getRows();
        List<Column> columns = fields.stream()
            .map(field -> new Column(field.getName(), field.getType(), alias))
            .collect(Collectors.toCollection(ArrayList::new));
        List<EvalRow> evalRows = rows.stream()
            .map(row -> {
                LinkedHashMap<String, Object> evalRow = new LinkedHashMap<String, Object>();
                for (int i = 0; i < fields.size(); i++) {
                    evalRow.put(columns.get(i).getFullName(), row.getValue(i));
                }
                return new EvalRow(evalRow);
            })
            .collect(Collectors.toCollection(ArrayList::new));
        return new EvalTable(columns, evalRows);
    }

    public static EvalTable addAlias(EvalTable table, String alias) {
        // Capture old column full names before alias change
        List<String> oldFullNames = table.columns.stream()
            .map(Column::getFullName)
            .collect(Collectors.toList());
        // Update column aliases
        table.columns.forEach(col -> col.setAlias(alias));
        // Capture new column full names after alias change
        List<String> newFullNames = table.columns.stream()
            .map(Column::getFullName)
            .collect(Collectors.toList());
        // Update field keys in each row, preserving order
        for (EvalRow row : table.rows) {
            LinkedHashMap<String, Object> oldFields = row.getFields();
            LinkedHashMap<String, Object> newFields = new LinkedHashMap<>();
            for (int i = 0; i < oldFullNames.size(); i++) {
                String oldKey = oldFullNames.get(i);
                String newKey = newFullNames.get(i);
                Object value = oldFields.get(oldKey);
                newFields.put(newKey, value);
            }
            row.fields = newFields;
        }
        return table;
    }

    public static EvalTable copyOf(EvalTable table) {
        // Deep copy columns
        List<Column> cols = new ArrayList<>();
        for (Column col : table.getColumns()) {
            cols.add(new Column(col.getName(), col.getType(), col.getAlias()));
        }
        // Deep copy rows
        List<EvalRow> rows = new ArrayList<>();
        for (EvalRow row : table.getRows()) {
            LinkedHashMap<String, Object> newFields = new LinkedHashMap<>();
            for (var entry : row.getFields().entrySet()) {
                newFields.put(entry.getKey(), entry.getValue());
            }
            rows.add(new EvalRow(newFields));
        }
        return new EvalTable(cols, rows);
    }
}
