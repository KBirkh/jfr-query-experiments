package me.bechberger.jfr.wrap.nodes;

public class UnaryOpNode extends AstConditional {
    private String operator;
    private AstConditional operand;

    /*
     * Represents a unary operation
     * In this case only used for signs of values
     * Contains the operator as a String and an operand
     */
    public UnaryOpNode(String operator, AstConditional operand) {
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

    @Override
    public void findAggregates(AstNode root) {
        if(operand != null) {
            operand.findAggregates(root);
        }
    }

    @Override
    public Object eval(Object row, AstNode root) {
        int toMultiply = operator == "-" ? -1 : 1;
        Object res = operand.eval(row, root);
        if(res instanceof Double) {
            double resNum = (Double) res;
            return resNum * toMultiply;
        }
        throw new IllegalArgumentException("The operand is not a number and no sign can be applied");
    }
    
}
