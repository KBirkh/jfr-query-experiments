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

    public Object eval(Object obj) {
        throw new UnsupportedOperationException("eval(Object obj) not implemented for " + this.getClass().getSimpleName());
    }


    public String getName() {
        return "AstNode abstract class";
    }
}
