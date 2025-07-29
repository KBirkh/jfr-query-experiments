package me.bechberger.jfr.wrap.nodes;

import me.bechberger.jfr.wrap.EvalTable;
import me.bechberger.jfr.wrap.Evaluator;

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

    @Override
    public Object eval() {
        Evaluator evaluator = Evaluator.getInstance();
        EvalTable table = evaluator.getFirstTable();
        table.rows = table.rows.parallelStream().limit(count).toList();
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
