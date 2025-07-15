package me.bechberger.jfr.wrap;

import java.util.ArrayList;
import java.util.List;

public class OrderByNode extends AstNode {
    private List<String> identifiers;

    public OrderByNode() {
        identifiers = new ArrayList<String>();
    }
    
    public void addOrder(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        this.identifiers.add(identifier);
    }
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(": ").append(identifierString()).append("\n");
        return sb.toString();
    }

    private String identifierString() {
        if (identifiers == null || identifiers.isEmpty()) {
            return "";
        }
        return String.join(", ", identifiers);
    }

}
