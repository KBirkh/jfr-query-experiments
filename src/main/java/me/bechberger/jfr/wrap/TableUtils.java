package me.bechberger.jfr.wrap;

import java.util.LinkedHashMap;
import java.util.List;

import me.bechberger.jfr.query.Field;
import me.bechberger.jfr.query.Row;
import me.bechberger.jfr.query.Table;

public class TableUtils {

    public static EvalTable toEvalTable(Table table) {
        List<Field> fields = table.getFields();
        List<Row> rows = table.getRows();
        List<Column> columns = fields.stream()
            .map(field -> new Column(field.getName(), field.getType()))
            .toList();
        List<EvalRow> evalRows = rows.stream()
            .map(row -> {
                LinkedHashMap<String, Object> evalRow = new LinkedHashMap<String, Object>();
                for (int i = 0; i < fields.size(); i++) {
                    evalRow.put(columns.get(i).getFullName(), row.getValue(i));
                }
                return new EvalRow(evalRow);
            })
            .toList();
        return new EvalTable(columns, evalRows);
    }

    public static EvalTable toEvalTable(Table table, String alias) {
        List<Field> fields = table.getFields();
        List<Row> rows = table.getRows();
        List<Column> columns = fields.stream()
            .map(field -> new Column(field.getName(), field.getType(), alias))
            .toList();
        List<EvalRow> evalRows = rows.stream()
            .map(row -> {
                LinkedHashMap<String, Object> evalRow = new LinkedHashMap<String, Object>();
                for (int i = 0; i < fields.size(); i++) {
                    evalRow.put(columns.get(i).getFullName(), row.getValue(i));
                }
                return new EvalRow(evalRow);
            })
            .toList();
        return new EvalTable(columns, evalRows);
    }
}
