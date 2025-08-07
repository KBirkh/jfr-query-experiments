package me.bechberger.jfr.wrap.nodes;

import me.bechberger.jfr.wrap.EvalTable;
import me.bechberger.jfr.wrap.Evaluator;

/*
 * This class represents a limit clause in the query
 * Only supports numbers, not expressions
 */
public class LimitNode extends AstNode {
    private int count;

    public LimitNode() {
        // Default constructor
    }
    public LimitNode(String count) {
        try {
            this.count = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid count value: " + count, e);
        }
        setCount(this.count);
    }

    public void setCount(int count) {
        this.count = count;
    }

    // Delegates the evaluation to the table to limit the rows
    @Override
    public Object eval(AstNode root) {
        Evaluator evaluator = Evaluator.getInstance();
        EvalTable table = evaluator.getTable(root);
        table.limit(count);
        return null;
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(": ").append(count);
        return sb.toString();
    }

}
