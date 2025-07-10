package me.bechberger.jfr.wrap;

public class ArithmeticNode extends AstNode {
    private ArithmeticNode left;
    private String op;
    private ArithmeticNode right;

    public ArithmeticNode() {

    }

    public void setLeft(ArithmeticNode left) {
        if (left == null) {
            throw new IllegalArgumentException("Left expression cannot be null");
        }
        this.left = left;
    }

    public void setOperator(String op) {
        if (op == null || op.isEmpty()) {
            throw new IllegalArgumentException("Operator cannot be null or empty");
        }
        this.op = op;
    }

    public void setRight(ArithmeticNode right) {
        if (right == null) {
            throw new IllegalArgumentException("Right expression cannot be null");
        }
        this.right = right;
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append(dent).append(this.getClass().getSimpleName()).append(":\n");
        if (left != null) {
            sb.append(left.toString(indent + 1)).append("\n");
        }
        sb.append(dent).append("  ").append(op).append("\n");
        if (right != null) {
            sb.append(right.toString(indent + 1)).append("\n");
        }
        return sb.toString();
    }

}
