package me.bechberger.jfr.wrap.nodes;

import java.util.List;

public class HavingNode extends AstNode {
    private List<AstNode> conditions;

    public HavingNode() {
        // Default constructor
    }
    public HavingNode(AstNode... conditions) {
        this();
        if (conditions == null || conditions.length == 0) {
            throw new IllegalArgumentException("Conditions cannot be null or empty");
        }
        for (AstNode condition : conditions) {
            addCondition(condition);
        }
    }

    public void addCondition(AstNode condition) {
        if (condition == null) {
            throw new IllegalArgumentException("Condition cannot be null");
        }
        if (conditions == null) {
            conditions = new java.util.ArrayList<AstNode>();
        }
        conditions.add(condition);
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        if (conditions != null) {
            for (AstNode condition : conditions) {
                sb.append(condition.toString(indent + 1));
            }
        }
        return sb.toString();
    }

}
