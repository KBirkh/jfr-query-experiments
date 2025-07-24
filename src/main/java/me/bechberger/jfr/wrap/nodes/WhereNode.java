package me.bechberger.jfr.wrap.nodes;

import me.bechberger.jfr.wrap.EvalTable;
import me.bechberger.jfr.wrap.Evaluator;

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

    public void eval() {
        Evaluator evaluator = Evaluator.getInstance();
        EvalTable evalTable = evaluator.getFirstTable();
        evalTable.rows = evalTable.getRows().stream()
            .filter(row -> ((ConditionNode) condition).eval(row))
            .toList();
    }

}
