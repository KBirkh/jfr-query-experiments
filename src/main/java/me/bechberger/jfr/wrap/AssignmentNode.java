package me.bechberger.jfr.wrap;

public class AssignmentNode extends AstNode {
    private String identifier;
    private ExpressionNode expression;
    private QueryNode query;

    public void setIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        this.identifier = identifier;
    }

    public void setQuery(QueryNode query) {
        if (query == null) {
            throw new IllegalArgumentException("Query cannot be null");
        }
        this.query = query;
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append(dent).append(this.getClass().getSimpleName()).append(":\n");
        sb.append(dent).append("  Identifier: ").append(identifier).append("\n");
        if (query != null) {
            sb.append(query.toString(indent + 1)).append("\n");
        }
        if (expression != null) {
            sb.append(expression.toString(indent + 1)).append("\n");
        }
        return sb.toString();
    }

}
