package me.bechberger.jfr.wrap;

public class ConditionNode extends AstNode {
    private String operator;
    private AstNode left;
    private AstNode right;

    public boolean isFirst = true;

    public void setOperator(String op) {
        if (op == null || op.isEmpty()) {
            throw new IllegalArgumentException("Operator cannot be null or empty");
        }
        this.operator = op;
        this.isFirst = false;
    }

    public void setLeft(AstNode arithmetic) {
        if (arithmetic == null) {
            throw new IllegalArgumentException("Left expression cannot be null");
        }
        this.left = arithmetic;
    }

    public void setRight(AstNode arithmetic) {
        if (arithmetic == null) {
            throw new IllegalArgumentException("Right expression cannot be null");
        }
        this.right = arithmetic;
    }
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        sb.append("\n").append(dent).append("  Operator: ").append(operator);
        sb.append("\n").append(dent).append("  Left: ").append(left.toString(indent + 1));
        sb.append("\n").append(dent).append("  Right: ").append(right.toString(indent + 1));
        return sb.toString();
    }

}
