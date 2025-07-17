package me.bechberger.jfr.wrap;

public class StringNode extends AstNode{
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
}
