package me.bechberger.jfr.wrap;

public class IdentifierNode extends AstNode {
    private String identifier;
    private String tableAlias;
    private boolean hasTableAlias;

    public IdentifierNode(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        this.identifier = identifier;
    }

    public IdentifierNode(String identifier, String tableAlias) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        this.identifier = identifier;
        this.tableAlias = tableAlias;
        this.hasTableAlias = tableAlias != null && !tableAlias.isEmpty();
    } 

    public IdentifierNode() {

    }

    public void setIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        this.identifier = identifier;
    }

    public void setTableAlias(String tableAlias) {
        if (tableAlias == null || tableAlias.isEmpty()) {
            throw new IllegalArgumentException("Table alias cannot be null or empty");
        }
        this.tableAlias = tableAlias;
        this.hasTableAlias = true;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getTableAlias() {
        return tableAlias;
    } 

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(": ").append(identifier);
        if (tableAlias != null && !tableAlias.isEmpty()) {
            sb.append("\n").append(dent).append("  ").append("Table: ").append(tableAlias);
        }
        return sb.toString();
    }
    
}
