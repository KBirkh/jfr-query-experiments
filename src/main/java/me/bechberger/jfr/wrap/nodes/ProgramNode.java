package me.bechberger.jfr.wrap.nodes;

import java.util.ArrayList;
import java.util.List;


public class ProgramNode extends AstNode {
    private List<AstNode> statements;

    public ProgramNode() {
        statements = new ArrayList<AstNode>();
    }

    public ProgramNode(AstNode... statements) {
        this();
        if (statements == null || statements.length == 0) {
            throw new IllegalArgumentException("Statements cannot be null or empty");
        }
        for (AstNode statement : statements) {
            addStatement(statement);
        }
    }

    public void addStatement(AstNode statement) {
        if(statement != null) {
            statements.add(statement);
        }
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append(dent).append(this.getClass().getSimpleName()).append(":");
        for (AstNode statement : statements) {
            sb.append(statement.toString(indent + 1));
        }
        return sb.toString();
    }
    @Override
    public void eval() {
        for (AstNode statement : statements) {
            statement.eval();
        }
    }
}
