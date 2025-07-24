package me.bechberger.jfr.wrap.nodes;

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
    
}
