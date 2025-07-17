package me.bechberger.jfr.wrap;

public class LimitNode extends AstNode {
    private AstNode count;

    public LimitNode() {
        // Default constructor
    }
    public LimitNode(AstNode count) {
        setCount(count);
    }

    public void setCount(AstNode count) {
        if (count == null) {
            throw new IllegalArgumentException("Count cannot be null");
        }
        if (this.count != null) {
            throw new IllegalStateException("LimitNode can only have one count");
        }
        this.count = count;
    }
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append(dent).append(this.getClass().getSimpleName()).append(": ").append(count.toString(indent + 1));
        return sb.toString();
    }

}
