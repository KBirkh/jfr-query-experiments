package me.bechberger.jfr.wrap.nodes;

import java.util.ArrayList;
import java.util.List;

import me.bechberger.jfr.wrap.EvalRow;
import me.bechberger.jfr.wrap.EvalTable;
import me.bechberger.jfr.wrap.Evaluator;

public class OrderByNode extends AstNode {
    private List<AstNode> identifiers;
    private List<String> directions;

    public OrderByNode() {
        identifiers = new ArrayList<AstNode>();
        directions = new ArrayList<String>();
    }

    public OrderByNode(AstNode[] identifiers, String[] directions) {
        this();
        if (identifiers == null || identifiers.length == 0) {
            throw new IllegalArgumentException("Identifiers cannot be null or empty");
        }
        for (AstNode identifier : identifiers) {
            addOrder(identifier);
        }

        for(String direction : directions) {
            addDirection(direction);
        }
    }
    
    public void addOrder(AstNode identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        this.identifiers.add(identifier);
    }

    public void addDirection(String direction) {
        if (direction == null || direction.isEmpty()) {
            throw new IllegalArgumentException("Direction cannot be null or empty");
        }
        this.directions.add(direction);
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(": ").append(identifierString(indent + 1)).append("\n");
        return sb.toString();
    }

    private String identifierString(int indent) {
        if (identifiers == null || identifiers.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < identifiers.size(); i++) {
            AstNode identifier = identifiers.get(i);
            sb.append(identifier.toString(indent));
            if (i < directions.size()) {
                sb.append(" ").append(directions.get(i));
            }
            if (i < identifiers.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    @Override
    public Object eval() {
        Evaluator evaluator = Evaluator.getInstance();
        EvalTable table = evaluator.getFirstTable();
        if (table == null) {
            throw new IllegalStateException("No EvalTable found to sort");
        }
        if (identifiers.isEmpty()) {
            throw new IllegalStateException("No identifiers to sort by");
        }
        List<EvalRow> rows = new ArrayList<>(table.rows);
        rows.sort((row1, row2) -> {
            for (int i = 0; i < identifiers.size(); i++) {
                AstNode identifier = identifiers.get(i);
                String direction = directions.size() > i ? directions.get(i) : "ASC";
                Object value1 = identifier.eval(row1);
                Object value2 = identifier.eval(row2);

                int comparison = compareValues(value1, value2);
                if (comparison != 0) {
                    return "DESC".equalsIgnoreCase(direction) ? -comparison : comparison;
                }
            }
            return 0; // If all identifiers are equal
        });
        table.rows = rows;
        evaluator.switchTable(table.getQuery(), table);
        return null;
    }
    
    public List<EvalRow> evalForPercentile() {
        Evaluator evaluator = Evaluator.getInstance();
        EvalTable table = evaluator.getFirstTable();
        if (table == null) {
            throw new IllegalStateException("No EvalTable found to sort");
        }
        if (identifiers.isEmpty()) {
            throw new IllegalStateException("No identifiers to sort by");
        }
        List<EvalRow> rows = new ArrayList<>(table.rows);
        rows.sort((row1, row2) -> {
            for (int i = 0; i < identifiers.size(); i++) {
                AstNode identifier = identifiers.get(i);
                String direction = directions.size() > i ? directions.get(i) : "ASC";
                Object value1 = identifier.eval(row1);
                Object value2 = identifier.eval(row2);

                int comparison = compareValues(value1, value2);
                if (comparison != 0) {
                    return "DESC".equalsIgnoreCase(direction) ? -comparison : comparison;
                }
            }
            return 0; // If all identifiers are equal
        });
        return rows;
    }

    private int compareValues(Object val1, Object val2) {
        if (val1 == null && val2 == null) {
            return 0;
        }
        if (val1 == null) {
            return -1; // Nulls are considered less than non-nulls
        }
        if (val2 == null) {
            return 1; // Non-nulls are considered greater than nulls
        }
        if (val1 instanceof Comparable && val2 instanceof Comparable) {
            return ((Comparable<Object>) val1).compareTo(val2);
        }
        throw new IllegalArgumentException("Values are not comparable: " + val1 + ", " + val2);
    }

}
