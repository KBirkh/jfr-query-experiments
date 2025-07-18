package me.bechberger.jfr.wrap;

import me.bechberger.jfr.query.Table;

public abstract class AstNode {

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        return sb.toString();
    }

    public Table eval() {
        return null;
    }
}
