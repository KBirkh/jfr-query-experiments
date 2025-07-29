package me.bechberger.jfr.wrap.nodes;

import me.bechberger.jfr.wrap.EvalRow;

public class OrNode extends AstConditional {
    private AstNode left;
    private AstNode right;

    public OrNode() {

    }

    public OrNode(AstNode left, AstNode right) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("Left and right nodes cannot be null");
        }
        this.left = left;
        this.right = right;
    }

    public void setLeft(AstNode left) {
        if (left == null) {
            throw new IllegalArgumentException("Left node cannot be null");
        }
        this.left = left;
    }

    public void setRight(AstNode right) {
        if (right == null) {
            throw new IllegalArgumentException("Right node cannot be null");
        }
        this.right = right;
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        sb.append("\n").append(dent).append("  Left: ").append(left.toString(indent + 1));
        sb.append("\n").append(dent).append("  Right: ").append(right.toString(indent + 1));
        return sb.toString();
    }

    @Override
    public Object eval(Object row) {
        if (left == null || right == null) {
            throw new IllegalStateException("Left and right nodes must be set before evaluation");
        }
        Boolean leftValue = (Boolean) left.eval(row);
        if (leftValue) {
            return true;
        }
        Boolean rightValue = (Boolean) right.eval(row);
        if (leftValue instanceof Boolean && rightValue instanceof Boolean) {
            return (Boolean) leftValue || (Boolean) rightValue;
        } else {
            throw new IllegalArgumentException("Both left and right nodes must evaluate to Boolean values");
        }
    }
    
}
