package me.bechberger.jfr.wrap.nodes;

public class SourceNode extends AstNode {
    private String name;
    private String alias;
    private AstNode subquery;
    public boolean isSubQuery;

    public void setSource(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name;
    }

    public SourceNode() {}

    public SourceNode(String name) {
        setSource(name);
    }

    public SourceNode(AstNode query) {
        if (query == null) {
            throw new IllegalArgumentException("Subquery cannot be null");
        }
        this.subquery = query;
        isSubQuery = true;
    }

    public SourceNode setAlias(String lexeme) {
        if (lexeme == null || lexeme.isEmpty()) {
            throw new IllegalArgumentException("Alias cannot be null or empty");
        }
        this.alias = lexeme;
        return this;
    }

    public void setSource(AstNode query) {
        if (query == null) {
            throw new IllegalArgumentException("Subquery cannot be null");
        }
        this.subquery = query;
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

    @Override
    public void eval() {
        if(isSubQuery) {
            if(alias == null || alias.isEmpty()) {
                subquery.eval();
            } else {
                subquery.eval(alias);
            }
        }
    }

}
