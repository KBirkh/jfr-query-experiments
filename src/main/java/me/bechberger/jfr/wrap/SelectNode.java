package me.bechberger.jfr.wrap;

import java.util.List;

public class SelectNode extends AstNode {
    private List<AstNode> columns;
    public boolean isStar;

    public SelectNode() {
        columns = new java.util.ArrayList<AstNode>();
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        if (isStar) {
            sb.append(" *");
        } else {
            for (AstNode column : columns) {
                sb.append(column.toString(indent + 1));
            }
        }
        return sb.toString();
    }

    public void addColumn(AstNode expression) {
        columns.add(expression);
    }

}
