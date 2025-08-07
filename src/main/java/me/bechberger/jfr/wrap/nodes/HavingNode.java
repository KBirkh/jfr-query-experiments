package me.bechberger.jfr.wrap.nodes;

import me.bechberger.jfr.wrap.EvalTable;
import me.bechberger.jfr.wrap.Evaluator;

/*
 * Represents a HAVING clause in the abstract syntax tree.
 * It contains a condition that filters groups after aggregation.
 * Used to specify conditions on aggregated data.
 */
public class HavingNode extends AstNode {
    private AstNode condition;

    public HavingNode() {
        // Default constructor
    }

    public HavingNode(AstNode condition) {
        this.condition = condition;
    }

    public void setCondition(AstNode condition) {
        this.condition = condition;
    }

    public void findAggregates(AstNode root) {
        if (condition != null) {
            condition.findAggregates(root);
        } else {
            throw new IllegalStateException("Condition has not been set for HavingNode");
        }
    }

    /*
     * Evaluates the condition for every row in the table to be filtered
     */
    @Override
    public Object eval(AstNode root) {
        Evaluator evaluator = Evaluator.getInstance();
        if (condition == null) {
            throw new IllegalStateException("Condition has not been set for HavingNode");
        }
        EvalTable table = evaluator.getTable(root);
        table.rows = table.getRows().parallelStream()
            .filter(row -> (Boolean) condition.eval(row, root) == true)
            .toList();
        return null;
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(": ");
        if (condition != null) {
            sb.append(condition.toString(indent + 1));
        } else {
            sb.append("No condition set");
        }
        return sb.toString();
    }

}
