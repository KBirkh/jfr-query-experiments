package me.bechberger.jfr.wrap.nodes;

import me.bechberger.jfr.wrap.EvalRow;

public class AndNode extends AstConditional {
    private AstNode left;
    private AstNode right;

    public AndNode(AstNode left, AstNode right) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("Left and right nodes cannot be null");
        }
        this.left = left;
        this.right = right;
    }

    public AndNode() {
        
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

    public Object eval(EvalRow row) {
        if (left == null || right == null) {
            throw new IllegalStateException("Left and right nodes must be set before evaluation");
        }
        Boolean leftValue = (Boolean) left.eval(row);
        if(!leftValue){ 
            return false;
        }   
        Object rightValue = right.eval(row);
        
        if (leftValue instanceof Boolean && rightValue instanceof Boolean) {
            return (Boolean) leftValue && (Boolean) rightValue;
        } else {
            throw new IllegalArgumentException("Both left and right nodes must evaluate to Boolean values");
        }
    }
    
}
