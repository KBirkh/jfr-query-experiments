package me.bechberger.jfr.wrap;

public class IdentifierNode extends AstNode {
    private String identifier;

    public IdentifierNode(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(": ").append(identifier);
        return sb.toString();
    }
    
}
