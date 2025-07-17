package me.bechberger.jfr.wrap;


public abstract class AstNode {

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        return sb.toString();
    }

    public String[][] eval() {
        return new String[][] {{"You somehow got an empty AST node, this is a bug in the code."}};
    }
}
