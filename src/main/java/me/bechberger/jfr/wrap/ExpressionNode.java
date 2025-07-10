package me.bechberger.jfr.wrap;

public class ExpressionNode extends AstNode {
    private String alias;
    private QueryNode query;
    private String identifier;
    private AstNode value;
    private ArithmeticNode arithmetic;

    public void setAlias(String alias) {
        if (alias == null || alias.isEmpty()) {
            throw new IllegalArgumentException("Alias cannot be null or empty");
        }
        this.alias = alias;
    }

    public void setIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        this.identifier = identifier;
    }

    public void setValue(AstNode value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null or empty");
        }
        this.value = value;
    }

    public void setArithmetic(ArithmeticNode arithmetic) {
        if (arithmetic == null) {
            throw new IllegalArgumentException("Arithmetic Node cannot be null");
        }
        this.arithmetic = arithmetic;
    }

    public void setQuery(QueryNode query) {
        if (query == null) {
            throw new IllegalArgumentException("Query cannot be null");
        }
        this.query = query;
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("  ".repeat(indent)).append(this.getClass().getSimpleName()).append(":");
        if (alias != null) {
            sb.append("\n").append("  ".repeat(indent + 1)).append("alias: ").append(alias);
        }
        /* if (function != null) {
            sb.append(" ".repeat(indent + 2)).append("function: ").append(function.toString(indent + 2)).append("\n");
        } */
        if (query != null) {
            sb.append("\n").append("  ".repeat(indent + 1)).append("query: ").append(query.toString(indent + 1));
        }
        if (identifier != null) {
            sb.append("\n").append("  ".repeat(indent + 1)).append("identifier: ").append(identifier);
        }
        if (value != null) {
            sb.append(value.toString(indent + 1 ));
        }
        if (arithmetic != null) {
            sb.append("\n").append("  ".repeat(indent + 1)).append("arithmetic: ").append(arithmetic.toString(indent + 1));
        }
        return sb.toString();
    }
}
