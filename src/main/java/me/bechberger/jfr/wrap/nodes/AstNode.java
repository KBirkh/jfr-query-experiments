package me.bechberger.jfr.wrap.nodes;

import me.bechberger.jfr.wrap.EvalRow;

public abstract class AstNode {

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        return sb.toString();
    }

    public Object eval(AstNode root) {
        return null;
    }

    public Object eval(Object obj, AstNode root) {
        throw new UnsupportedOperationException("eval(Object obj, AstNode root) not implemented for " + this.getClass().getSimpleName());
    }


    public String getName() {
        return "AstNode abstract class";
    }

    public void findAggregates(AstNode root) {
        throw new UnsupportedOperationException("findAggregates(AstNode root) not implemented for " + this.getClass().getSimpleName());
    }
}
