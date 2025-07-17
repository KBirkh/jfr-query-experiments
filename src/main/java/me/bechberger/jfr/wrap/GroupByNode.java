package me.bechberger.jfr.wrap;

import java.util.ArrayList;
import java.util.List;

public class GroupByNode extends AstNode {
    private List<AstNode> identifiers;

    public GroupByNode(AstNode... identifiers) {
        if (identifiers == null || identifiers.length == 0) {
            throw new IllegalArgumentException("Identifiers cannot be null or empty");
        }
        this.identifiers = List.of(identifiers);
    }

    public GroupByNode() {
        identifiers = new ArrayList<>();
    }
    

    public void addGroup(AstNode identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        identifiers.add(identifier);
    }
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        if (identifiers == null || identifiers.isEmpty()) {
            sb.append(" No identifiers");
        } else {
            for (AstNode identifier : identifiers) {
                sb.append("\n").append(dent).append("  Identifier: ").append(identifier.toString(indent + 1));
            }
        }
        return sb.toString();
    }

}
