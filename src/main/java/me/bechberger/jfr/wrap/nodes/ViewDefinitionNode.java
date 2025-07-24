package me.bechberger.jfr.wrap.nodes;

public class ViewDefinitionNode extends AstNode {
    private String name;
    private QueryNode query;

    public ViewDefinitionNode(String name, AstNode query) {
        setName(name);
        setQuery(query);
    }

    public ViewDefinitionNode() {
        
    }
    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name;
    }

    public void setQuery(AstNode query) {
        if (query == null) {
            throw new IllegalArgumentException("Query cannot be null");
        }
        this.query = (QueryNode) query;
    }
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName());
        sb.append("\n").append(dent).append("  Name: ").append(name);
        sb.append("\n").append(dent).append("  Query: ").append(query.toString(indent + 1));
        return sb.toString();
    }

}
