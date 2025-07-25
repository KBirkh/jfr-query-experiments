package me.bechberger.jfr.wrap.nodes;

import me.bechberger.jfr.wrap.EvalRow;

public abstract class AstNode {

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        return sb.toString();
    }

    public Object eval() {
        return null;
    }

    public Object eval(String alias) {
        return null;
    }

    public Object eval(EvalRow row) {
        return false;
    }
}
