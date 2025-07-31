package me.bechberger.jfr.wrap.nodes;

public class ArithmeticNode extends AstConditional {
    private AstNode left;
    private String op;
    private AstNode right;
    private String alias;

    public ArithmeticNode() {

    }

    public ArithmeticNode(AstNode left, String op, AstNode right) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("Left and right expressions cannot be null");
        }
        if (op == null || op.isEmpty()) {
            throw new IllegalArgumentException("Operator cannot be null or empty");
        }
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public void setAlias(String alias) {
        if (alias == null || alias.isEmpty()) {
            throw new IllegalArgumentException("Alias cannot be null or empty");
        }
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public void setLeft(AstNode left) {
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

    public void setRight(AstNode right) {
        if (right == null) {
            throw new IllegalArgumentException("Right expression cannot be null");
        }
        this.right = right;
    }

    @Override
    public void findAggregates(AstNode root) {
        if(left != null) {
            left.findAggregates(root);
        }
        if(right != null) {
            right.findAggregates(root);
        }
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
