package me.bechberger.jfr.wrap;


public abstract class AstNode {
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append("  ".repeat(indent));
        sb.append("This is the abstracted AstNode");
        return sb.toString();
    } 
}
