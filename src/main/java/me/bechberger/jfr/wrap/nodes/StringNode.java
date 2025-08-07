package me.bechberger.jfr.wrap.nodes;

/*
 * Represents a String
 */
public class StringNode extends AstConditional {
    private String value;


    public StringNode(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("String value cannot be null or empty");
        }
        this.value = value;
    }

    public StringNode() {
        // Default constructor for deserialization or other purposes
    }
    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /*
     * returns the String value
     */
    @Override
    public Object eval(Object row, AstNode root) {
        if (value == null) {
            throw new IllegalStateException("String value has not been set");
        }
        return value;
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(": ").append(value);
        return sb.toString();
    }

    @Override
    public String getName() {
        return value != null ? value : "StringNode with no value";
    }

    /*
     * Is leaf of the conditional clauses and is not an aggregate
     * function -> do nothing
     */
    @Override
    public void findAggregates(AstNode root) {
        
    }
}
