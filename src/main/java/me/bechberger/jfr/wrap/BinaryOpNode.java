package me.bechberger.jfr.wrap;

public class BinaryOpNode extends AstNode {
    private String operator;
    private AstNode leftOperand;
    private AstNode rightOperand;

    public BinaryOpNode(String operator, AstNode leftOperand, AstNode rightOperand) {
        if (operator == null || operator.isEmpty()) {
            throw new IllegalArgumentException("Operator cannot be null or empty");
        }
        if (leftOperand == null) {
            throw new IllegalArgumentException("Left operand cannot be null");
        }
        if (rightOperand == null) {
            throw new IllegalArgumentException("Right operand cannot be null");
        }
        this.operator = operator;
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(": ").append(operator)
          .append(" ").append(leftOperand.toString(indent + 1)).append(rightOperand.toString(indent + 1));
        return sb.toString();
    }
}
