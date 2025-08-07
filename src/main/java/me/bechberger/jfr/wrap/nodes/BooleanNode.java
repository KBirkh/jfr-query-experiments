package me.bechberger.jfr.wrap.nodes;

/*
 * This node represents a boolean value
 */
public class BooleanNode extends AstConditional {
    private boolean value;

    public void setValue(boolean value) {
        this.value = value;
    }

    public BooleanNode() {

    }

    public BooleanNode(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public void findAggregates(AstNode root) {
        // No aggregates to find in a BooleanNode
    }

    @Override
    public Object eval(Object row, AstNode root) {
        return value;
    }
}