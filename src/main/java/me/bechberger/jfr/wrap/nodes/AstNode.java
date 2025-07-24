package me.bechberger.jfr.wrap.nodes;

import me.bechberger.jfr.wrap.EvalRow;

public abstract class AstNode {

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        return sb.toString();
    }

    public void eval() {

    }

    public void eval(String alias) {
        
    }

    public Object eval(EvalRow row) {
        return false;
    }
}
