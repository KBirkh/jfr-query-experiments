package me.bechberger.jfr.wrap;

import java.util.ArrayList;
import java.util.List;

public class ProgramNode extends AstNode {
    private List<AstNode> statements;

    public ProgramNode() {
        statements = new ArrayList<AstNode>();
    }

    public void addStatement(AstNode statement) {
        statements.add(statement);
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        for (AstNode statement : statements) {
            sb.append(statement.toString(indent + 1));
        }
        return sb.toString();
    }
}
