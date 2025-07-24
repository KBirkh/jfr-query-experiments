package me.bechberger.jfr.wrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Evaluator {
    private static Evaluator instance;
    private HashMap<String, EvalTable> tables;

    private Evaluator() {
        this.tables = new HashMap<String, EvalTable>();
    }

    public void addTable(EvalTable table, String query) {
        tables.put(query, table);
    }

    public void addToTable(EvalTable table, String query) {
        if(tables.isEmpty()) {
            addTable(table, query);
        } else {
            EvalTable tab1 = tables.values().stream().findFirst().orElse(null);
            tables.clear();
            ArrayList<Column> resCol = new ArrayList<Column>();
            for(Column col : tab1.getColumns()) {
                resCol.add(col);
            }
            for(Column col : table.getColumns()) {
                resCol.add(col);
            }
            EvalTable res = new EvalTable(resCol, new ArrayList<EvalRow>());
            for(EvalRow a : tab1.rows) {
                for(EvalRow b : table.rows) {
                    EvalRow newRow = new EvalRow();
                    for(Map.Entry<String, Object> entry : a.getFields().entrySet()) {
                        newRow.addField(entry.getKey(), entry.getValue());
                    }
                    for(Map.Entry<String, Object> entry : b.getFields().entrySet()) {
                        newRow.addField(entry.getKey(), entry.getValue());
                    }
                    res.addRow(newRow);
                }
            }

            tables.put(query, res);

        }
    
    }

    public EvalTable getTable(String query) {
        return tables.get(query);
    }

    public EvalTable getFirstTable() {
        if(tables.isEmpty()) {
            return null;
        }
        return tables.values().stream().findFirst().orElse(null);
    }
    

    public static Evaluator getInstance() {
        if(instance == null) {
            instance = new Evaluator();
        }
        return instance;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, EvalTable> entry : tables.entrySet()) {
            EvalTable table = entry.getValue();
            sb.append("Rows:\n");
            for (EvalRow row : table.rows) {
                sb.append(row).append("\n");
            }
            sb.append("\nQuery: ").append(entry.getKey());
            sb.append("\nColumns: ");
            for (Column column : table.columns) {
                sb.append(column.getFullName()).append(", ");
            }
        }
        return sb.toString();
    }
}

