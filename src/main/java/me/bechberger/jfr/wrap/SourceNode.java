package me.bechberger.jfr.wrap;

public class SourceNode extends AstNode {
    private String name;
    private String alias;
    private QueryNode subquery;
    public boolean isSubQuery;

    public void setSource(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name;
    }

    public void setAlias(String lexeme) {
        if (lexeme == null || lexeme.isEmpty()) {
            throw new IllegalArgumentException("Alias cannot be null or empty");
        }
        this.alias = lexeme;
    }

    public void setSource(AstNode query) {
        if (query == null) {
            throw new IllegalArgumentException("Subquery cannot be null");
        }
        this.subquery = (QueryNode) query;
        isSubQuery = true;
    }
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        if (isSubQuery) {
            sb.append("\n").append(dent).append("  Subquery: ").append(subquery.toString(indent + 2));
        } else {
            sb.append("\n").append(dent).append("  Name: ").append(name);
        }
        if (alias != null) {
            sb.append("\n").append(dent).append("  Alias: ").append(alias);
        }
        return sb.toString();
    }

}
