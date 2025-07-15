package me.bechberger.jfr.wrap;

public class StringNode extends AstNode{
    private String value;

    public void setValue(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
