package me.bechberger.jfr.wrap.nodes;

import me.bechberger.jfr.wrap.EvalRow;

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

    public Boolean eval(EvalRow row) {
        return value;
    }
}