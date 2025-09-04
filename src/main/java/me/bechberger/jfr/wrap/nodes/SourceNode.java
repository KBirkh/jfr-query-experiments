package me.bechberger.jfr.wrap.nodes;

import me.bechberger.jfr.wrap.EvalTable;
import me.bechberger.jfr.wrap.Evaluator;
import me.bechberger.jfr.wrap.TableUtils;

/*
 * Represents a source in the AST
 * Can be a table or a subquery
 * and can have an alias
 */
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

    /*
     * Evaluates the source node
     * If it is a subquery, it evaluates the subquery
     * If it is not a subquery -> Some table is assigned to the identifier
     * evaluate the assignment and create a copy of the result to preserve
     * the table for the assignment.
     * Afterwards add the alis if present and save in the evaluator
     */
    @Override
    public Object eval(AstNode root) {
        if(isSubQuery) {
            if(alias == null || alias.isEmpty()) {
                subquery.eval(root);
            } else {
                subquery.eval(alias, root);
            }
        } else {
            Evaluator evaluator = Evaluator.getInstance();
            AstNode assignment = (AstNode) evaluator.getAssignment(name);
            if(assignment != null) {
                assignment.eval(assignment);
                EvalTable copy = TableUtils.copyOf(evaluator.getTable(assignment));
                if(alias != null && !alias.isEmpty()) {
                    evaluator.addTable(TableUtils.addAlias(copy, alias), root);
                } else {
                    evaluator.addTable(copy, root);
                }
                evaluator.moveDataSource(assignment);
            } else {
                evaluator.addTodo(name, evaluator.getCurrentRoot());
                return -1; // Indicating that this is a source node to be evaluated later
            }
        }
        return 0;
    }

}
