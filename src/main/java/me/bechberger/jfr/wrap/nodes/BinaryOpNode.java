package me.bechberger.jfr.wrap.nodes;

import me.bechberger.jfr.wrap.Evaluator;

/*
 * Represents a binary operation node in the abstract syntax tree.
 * It contains an operator and two operands, which are also AST nodes.
 * The operator is a string that defines the operation (e.g., "+", "-", "*", ...)
 * 
 */
public class BinaryOpNode extends AstConditional {
    private String operator;
    private AstConditional leftOperand;
    private AstConditional rightOperand;

    public BinaryOpNode(String operator, AstConditional leftOperand, AstConditional rightOperand) {
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

    /*
     * This node is also present in the findAggregates stage of evaluation
     */
    @Override
    public void findAggregates(AstNode root) {
        if (leftOperand != null) {
            leftOperand.findAggregates(root);
        }
        if (rightOperand != null) {
            rightOperand.findAggregates(root);
        }
    }

    @Override
    public void evalNonAggregates(AstNode root) {
        Evaluator evaluator = Evaluator.getInstance();
        if(leftOperand != null && leftOperand instanceof FunctionNode) {
            evaluator.addNonAggregate(leftOperand, root);
        } else if(leftOperand != null) {
            leftOperand.evalNonAggregates(root);
        }
        if(rightOperand != null && rightOperand instanceof FunctionNode) {
            evaluator.addNonAggregate(rightOperand, root);
        } else if(rightOperand != null) {
            rightOperand.evalNonAggregates(root);
        }
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
