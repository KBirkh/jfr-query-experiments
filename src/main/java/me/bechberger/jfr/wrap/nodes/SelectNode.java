package me.bechberger.jfr.wrap.nodes;

import java.util.ArrayList;
import java.util.List;

import me.bechberger.jfr.wrap.Column;
import me.bechberger.jfr.wrap.EvalTable;
import me.bechberger.jfr.wrap.Evaluator;

/*
 * Represents a SELECT statement in the abstract syntax tree.
 * It contains a list of columns to be selected, which can be either specific columns or a wildcard (*).
 * The isStar flag indicates whether the selection is a wildcard selection.
 * The columns are stored as a list of AstNode objects, which can represent various expressions.
 * Used in the last stage of evaluation when a projection is built
 */
public class SelectNode extends AstNode {
    private List<AstNode> columns;
    public boolean isStar;

    public SelectNode() {
        columns = new java.util.ArrayList<AstNode>();
    }

    public SelectNode(AstNode... columns) {
        this.columns = new java.util.ArrayList<AstNode>();
        if (columns == null || columns.length == 0) {
            throw new IllegalArgumentException("Columns cannot be null or empty");
        }
        for (AstNode column : columns) {
            addColumn(column);
        }
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        if (isStar) {
            sb.append(" *");
        } else {
            for (AstNode column : columns) {
                sb.append(column.toString(indent + 1));
            }
        }
        return sb.toString();
    }

    public void addColumn(AstNode expression) {
        columns.add(expression);
    }
    
    /*
     * Entrypoint for the findAggregates phase
     * Delegates search for aggregate functions to the columns
     * If the query has a HAVING clause, it will also search for aggregates there
     */
    public void findAggregates(AstNode root) {
        Evaluator evaluator = Evaluator.getInstance();
        for (AstNode column : columns) {
            if (column instanceof FunctionNode) {
                FunctionType type = ((FunctionNode) column).getType();
                if(type != FunctionType.BEFORE_GC && type != FunctionType.AFTER_GC && type != FunctionType.NEAR_GC) {
                    evaluator.addAggregate(column, root);
                }
            }
        }
        if(root instanceof QueryNode) {
            QueryNode queryNode = (QueryNode) root;
            if(queryNode.getHaving() != null) {
                ((HavingNode) queryNode.getHaving()).findAggregates(root);
            }
        }
    }

    /*
     * Also searches for non-aggregates like the GC correlations
     */
    public void evalNonAggregates(AstNode root) {
        Evaluator evaluator = Evaluator.getInstance();
        for (AstNode column : columns) {
            if (column instanceof FunctionNode) {
                FunctionType type = ((FunctionNode) column).getType();
                if(type == FunctionType.BEFORE_GC || type == FunctionType.AFTER_GC || type == FunctionType.NEAR_GC) {
                    evaluator.addNonAggregate(column, root);
                }
            }
        }
        evaluator.evalNonAggregates(root);
    }

    /*
     * Evaluates the projection
     * If the selection is a wildcard (*), it keeps all columns in the EvalTable.
     */
    @Override
    public Object eval(AstNode root) {
        Evaluator evaluator = Evaluator.getInstance();
        EvalTable table = evaluator.getTable(root);
        List<Column> keep = new ArrayList<>();
        if (isStar) {
            // If it's a star selection, keep all columns
            keep.addAll(table.getColumns());
            return null;
        } else {
            for(AstNode col : columns) {
                table.getColumns().stream()
                    .filter(c -> c.getName().equals(col.getName()))
                    .findFirst()
                    .ifPresent(keep::add);
            }
        }
        List<Column> allColumns = List.copyOf(table.getColumns());
        for(Column col : allColumns) {
            if(!keep.contains(col)) {
                evaluator.removeCol(col, root);
            }
        }
        if(table.getColumns().isEmpty()) {
            throw new IllegalStateException("No columns left after evaluation, cannot proceed");
        }
        return null;
    }


}
