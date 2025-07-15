package me.bechberger.jfr.wrap;

public class GroupByNode extends AstNode {
    private String identifier;

    public void addGroup(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        this.identifier = identifier;
    }
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append(dent).append(this.getClass().getSimpleName()).append(": ").append(identifier).append("\n");
        return sb.toString();
    }

}
