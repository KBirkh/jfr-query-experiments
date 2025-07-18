package me.bechberger.jfr.wrap;

public class UnaryOpNode extends AstNode {
    private String operator;
    private AstNode operand;

    public UnaryOpNode(String operator, AstNode operand) {
        if (operator == null || operator.isEmpty()) {
            throw new IllegalArgumentException("Operator cannot be null or empty");
        }
        if (operand == null) {
            throw new IllegalArgumentException("Operand cannot be null");
        }
        this.operator = operator;
        this.operand = operand;
    }
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(": ").append(operator).append(" ").append(operand.toString(indent + 1));
        return sb.toString();
    }
    
}
