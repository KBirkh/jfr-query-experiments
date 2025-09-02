package me.bechberger.jfr.wrap.nodes;

import jdk.jfr.consumer.RecordedThread;
import me.bechberger.jfr.wrap.Evaluator;
import me.bechberger.jfr.wrap.TokenType;

/*
 * Represents a condition in the abstract syntax tree.
 * It contains an operator and two operands (left and right).
 * The operator can be one of the comparison operators like EE, NEQ, GT, LT, GE, LE.
 * Used in the WHERE clause of a query to filter results based on conditions.
 * Always returns a boolean
 */
public class ConditionNode extends AstNode {
    private TokenType operator;
    private AstConditional left;
    private AstConditional right;

    public boolean isFirst = true;

    public void setOperator(TokenType op) {
        if (op == null) {
            throw new IllegalArgumentException("Operator cannot be null or empty");
        }
        this.operator = op;
        this.isFirst = false;
    }

    public ConditionNode() {

    }

    public ConditionNode(TokenType operator, AstConditional left, AstConditional right) {
        if (operator == null) {
            throw new IllegalArgumentException("Operator cannot be null or empty");
        }
        if (left == null) {
            throw new IllegalArgumentException("Left expression cannot be null");
        }
        if (right == null) {
            throw new IllegalArgumentException("Right expression cannot be null");
        }
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    public void setLeft(AstConditional arithmetic) {
        if (arithmetic == null) {
            throw new IllegalArgumentException("Left expression cannot be null");
        }
        this.left = arithmetic;
    }

    public void setRight(AstConditional arithmetic) {
        if (arithmetic == null) {
            throw new IllegalArgumentException("Right expression cannot be null");
        }
        this.right = arithmetic;
    }
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        sb.append("\n").append(dent).append("  Operator: ").append(operator);
        sb.append("\n").append(dent).append("  Left: ").append(left.toString(indent + 1));
        sb.append("\n").append(dent).append("  Right: ").append(right.toString(indent + 1));
        return sb.toString();
    }

    @Override
    public void evalNonAggregates(AstNode root) {
        Evaluator evaluator = Evaluator.getInstance();
        if(left != null && left instanceof FunctionNode) {
            evaluator.addNonAggregate(left, root);
        }
        if(right != null && right instanceof FunctionNode) {
            evaluator.addNonAggregate(right, root);
        }
    }

    /*
     * This method evaluates the condition node.
     * It evaluates the left and right operands and compares them based on the operator.
     * If the operator is EE, it checks for equality.
     * If the operator is NEQ, it checks for inequality.
     * If the operator is GT, it checks if the left operand is greater than the right
     * operand.
     * If the operator is LT, it checks if the left operand is less than the right
     * operand.
     * If the operator is GE, it checks if the left operand is greater than or equal
     * to the right operand.
     * If the operator is LE, it checks if the left operand is less than or equal
     * to the right operand.
     * If either operand is null it returns false
     * Special case for RecordedThread and Integers
     */
    @Override
    public Object eval(Object row, AstNode root) {
        if (left == null || right == null || operator == null) {
            throw new IllegalStateException("ConditionNode is not fully initialized");
        }

        Object leftValue = left.eval(row, root);
        Object rightValue = right.eval(row, root);

        if(leftValue.getClass() != rightValue.getClass() && !(leftValue instanceof Number || rightValue instanceof Number)) {
            throw new IllegalArgumentException(leftValue.getClass().getSimpleName() + " and " + rightValue.getClass().getSimpleName() + " types are not comparable");
        }

        if(leftValue instanceof RecordedThread) {
            leftValue = ((RecordedThread) leftValue).getOSName();
        }  else if(leftValue instanceof Integer) {
            leftValue = Double.parseDouble(leftValue.toString());
        }
        if (leftValue == null || rightValue == null) {
            return false; // Handle null values as false
        }
        

        switch (operator) {
            case TokenType.EE:
                return leftValue.equals(rightValue);
            case TokenType.NEQ:
                return !leftValue.equals(rightValue);
            case TokenType.GT:
                return ((Comparable<Object>) leftValue).compareTo(rightValue) > 0;
            case TokenType.LT:
                return ((Comparable<Object>) leftValue).compareTo(rightValue) < 0;
            case TokenType.GE:
                return ((Comparable<Object>) leftValue).compareTo(rightValue) >= 0;
            case TokenType.LE:
                return ((Comparable<Object>) leftValue).compareTo(rightValue) <= 0;
            default:
                throw new UnsupportedOperationException("Unsupported operator: " + operator);
        }
    }

    @Override
    public void findAggregates(AstNode root) {
        if (left != null) {
            left.findAggregates(root);
        }
        if (right != null) {
            right.findAggregates(root);
        }
    }

}
