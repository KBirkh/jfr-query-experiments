package me.bechberger.jfr.wrap;

public class ConditionNode extends AstNode {
    private String operator;
    private ConditionNode conditionTail;
    private ExpressionNode left;
    private ExpressionNode right;
    private ConditionNode condition;

    public boolean isFirst = true;

    public void setOperator(String op) {
        if (op == null || op.isEmpty()) {
            throw new IllegalArgumentException("Operator cannot be null or empty");
        }
        this.operator = op;
        this.isFirst = false;
    }

    public void setTail(ConditionNode conditionTail) {
        this.conditionTail = conditionTail;
    }

    public void setLeft(ExpressionNode expression) {
        if (expression == null) {
            throw new IllegalArgumentException("Left expression cannot be null");
        }
        this.left = expression;
    }

    public void setRight(ExpressionNode expression) {
        if (expression == null) {
            throw new IllegalArgumentException("Right expression cannot be null");
        }
        this.right = expression;
    }

    public void setCondition(ConditionNode condition) {
        if (condition == null) {
            throw new IllegalArgumentException("Condition cannot be null");
        }
        this.condition = condition;
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName());
        if (left != null) {
            sb.append(left.toString(indent + 1));
        }
        if (operator != null) {
            sb.append("\n").append(dent).append("  Operator: ").append(operator);
        }
        if (right != null) {
            sb.append(right.toString(indent + 1));
        }
        if (condition != null) {
            sb.append(condition.toString(indent + 1));
        }
        if (conditionTail != null) {
            sb.append(conditionTail.toString(indent + 1));
        }
        return sb.toString();
    }

}
