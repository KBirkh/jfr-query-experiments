package me.bechberger.jfr.wrap;

public class NumberNode extends AstNode {
    private String value;

    public NumberNode(String value) {
        this.value = value;
    }
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(": ").append(value);
        return sb.toString();
    }
}
