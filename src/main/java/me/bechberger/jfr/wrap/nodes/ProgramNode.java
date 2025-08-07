package me.bechberger.jfr.wrap.nodes;

import java.util.ArrayList;
import java.util.List;

import me.bechberger.jfr.wrap.Evaluator;

/*
 * Represents the root of the tree
 * and contains all the queries or assignments
 */
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

    /*
     * During evaluation evaluates all statements seperately
     * as long as the isEvaluated flag is not set for the statement
     */
    public Object eval() {
        Evaluator evaluator = Evaluator.getInstance();
        for (AstNode statement : statements) {
            if(!statement.isEvaluated) {
                evaluator.setRoot(statement);
                statement.eval(statement);
            }
        }
        evaluator.evalTodos();
        return null;
    }
    
}
