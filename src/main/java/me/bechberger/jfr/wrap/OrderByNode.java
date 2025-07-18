package me.bechberger.jfr.wrap;

import java.util.ArrayList;
import java.util.List;

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

}
