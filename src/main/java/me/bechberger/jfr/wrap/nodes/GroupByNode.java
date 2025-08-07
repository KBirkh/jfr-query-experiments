package me.bechberger.jfr.wrap.nodes;

import java.util.ArrayList;
import java.util.List;

import me.bechberger.jfr.wrap.EvalRow;
import me.bechberger.jfr.wrap.Evaluator;

/*
 * Represents a GROUP BY clause in the abstract syntax tree.
 * It contains a list of identifiers (columns or expressions) by which the results are grouped.
 * Used in the GROUP BY clause of a query to aggregate results based on specified identifiers.
 */
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
                sb.append(identifier.toString(indent + 1));
            }
        }
        return sb.toString();
    } 

    /*
     * Does not actually perform the grouping
     * Just tells the Evaluator by which columns to group by
     */
    public Object eval(EvalRow row, AstNode root) {
        Evaluator evaluator = Evaluator.getInstance();
        for(AstNode identifier : identifiers) {
            evaluator.addGrouping(identifier, root);
        }
        return null;
    }

}
