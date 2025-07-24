package me.bechberger.jfr.wrap.nodes;

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
}