package me.bechberger.jfr.wrap.nodes;

import me.bechberger.jfr.wrap.EvalRow;

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

    public Object eval(EvalRow row) {
        return value != null ? Double.parseDouble(value) : null;
    }
}
