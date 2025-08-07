package me.bechberger.jfr.wrap.nodes;

import java.util.ArrayList;
import java.util.stream.Collectors;

import me.bechberger.jfr.wrap.EvalTable;
import me.bechberger.jfr.wrap.Evaluator;

/*
 * Represents a Where clause and contains the condition tree
 */
public class WhereNode extends AstNode {
    private AstNode condition;

    public WhereNode() {

    }

    public WhereNode(AstNode condition) {
        if (condition == null) {
            throw new IllegalArgumentException("Condition cannot be null");
        }
        this.condition = condition;
    }

    public void setCondition(AstNode condition) {
        if (this.condition == null) {
            this.condition = condition;
        } else {
            throw new IllegalStateException("WhereNode can only have one condition");
        }
    }
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        if (condition != null) {
            sb.append(condition.toString(indent + 1));
        }
        return sb.toString();
    }

    /*
     * Filters all rows of the table for this tree
     * by whether the condition evaluates to true for the row
     */
    @Override
    public Object eval(AstNode root) {
        Evaluator evaluator = Evaluator.getInstance();
        EvalTable evalTable = evaluator.getTable(root);
        evalTable.rows = evalTable.getRows().parallelStream()
            .filter(row -> (Boolean) condition.eval(row, root) != null && (Boolean) condition.eval(row, root))
            .collect(Collectors.toCollection(ArrayList::new));
        return null;
    }

}
