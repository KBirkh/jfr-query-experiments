package me.bechberger.jfr.wrap;

import java.util.List;

import me.bechberger.jfr.wrap.nodes.AstNode;

public class EvalTable {
    public List<Column> columns;
    public List<EvalRow> rows;
    private AstNode root;

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
        columns.add(column);
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

    public List<EvalRow> getRows() {
        return rows;
    }
}
