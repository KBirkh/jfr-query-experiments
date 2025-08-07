package me.bechberger.jfr.wrap.nodes;

/*
 * Represents a number with a value
 */
public class NumberNode extends AstConditional {
    private String value;

    public NumberNode(String value) {
        this.value = value;
    }
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(": ").append(value);
        return sb.toString();
    }

    // Returns the value as a double
    @Override
    public Object eval(Object row, AstNode root) {
        return value != null ? Double.parseDouble(value) : null;
    }

    @Override
    public void findAggregates(AstNode root) {
        
    }

    public String getType() {
        return "Number";
    }

    @Override
    public String getName() {
        return value;
    }
}
