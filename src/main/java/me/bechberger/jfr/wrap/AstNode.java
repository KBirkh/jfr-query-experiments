package me.bechberger.jfr.wrap;


public abstract class AstNode {

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        return sb.toString();
    }
}
