package me.bechberger.jfr.wrap;

public class BooleanNode extends AstNode {
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
}