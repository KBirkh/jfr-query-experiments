package me.bechberger.jfr.wrap.nodes;

import jdk.jfr.consumer.RecordedThread;
import me.bechberger.jfr.wrap.EvalRow;
import me.bechberger.jfr.wrap.TokenType;

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

    public Boolean eval(EvalRow row) {
        if (left == null || right == null || operator == null) {
            throw new IllegalStateException("ConditionNode is not fully initialized");
        }

        Object leftValue = left.eval(row);
        Object rightValue = right.eval(row);

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

}
