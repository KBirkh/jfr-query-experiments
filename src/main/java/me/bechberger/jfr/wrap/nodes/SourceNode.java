package me.bechberger.jfr.wrap.nodes;

import me.bechberger.jfr.wrap.Evaluator;
import me.bechberger.jfr.wrap.TableUtils;

public class SourceNode extends AstNode {
    private String name;
    private String alias;
    private AstNode subquery;
    public boolean isSubQuery;

    public void setSource(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name;
    }

    public SourceNode() {}

    public SourceNode(String name) {
        setSource(name);
    }

    public SourceNode(AstNode query) {
        if (query == null) {
            throw new IllegalArgumentException("Subquery cannot be null");
        }
        this.subquery = query;
        isSubQuery = true;
    }

    public SourceNode setAlias(String lexeme) {
        if (lexeme == null || lexeme.isEmpty()) {
            throw new IllegalArgumentException("Alias cannot be null or empty");
        }
        this.alias = lexeme;
        return this;
    }

    public void setSource(AstNode query) {
        if (query == null) {
            throw new IllegalArgumentException("Subquery cannot be null");
        }
        this.subquery = query;
        isSubQuery = true;
    }
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        if (isSubQuery) {
            sb.append("\n").append(dent).append("  Subquery: ").append(subquery.toString(indent + 2));
        } else {
            sb.append("\n").append(dent).append("  Name: ").append(name);
        }
        if (alias != null) {
            sb.append("\n").append(dent).append("  Alias: ").append(alias);
        }
        return sb.toString();
    }

    @Override
    public Object eval(AstNode root) {
        if(isSubQuery) {
            if(alias == null || alias.isEmpty()) {
                subquery.eval(root);
            } else {
                subquery.eval(root);
            }
        } else {
            Evaluator evaluator = Evaluator.getInstance();
            AstNode assignment = (AstNode) evaluator.getAssignment(name);
            if(assignment != null) {
                assignment.eval(assignment);
                if(alias != null && !alias.isEmpty()) {
                    evaluator.switchTable(assignment, TableUtils.addAlias(evaluator.getTable(assignment), alias));
                }
            } else {
                evaluator.addTodo(name, evaluator.getCurrentRoot());
            }
        }
        return null;
    }

}
