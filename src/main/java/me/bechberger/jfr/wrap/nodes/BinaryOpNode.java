package me.bechberger.jfr.wrap.nodes;

import java.time.Duration;

import me.bechberger.jfr.wrap.Evaluator;

/*
 * Represents a binary operation node in the abstract syntax tree.
 * It contains an operator and two operands, which are also AST nodes.
 * The operator is a string that defines the operation (e.g., "+", "-", "*", ...)
 * 
 */
public class BinaryOpNode extends AstConditional {
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

    /*
     * #TODO: handle nulls, maybe implement special type for gc-correlation
     */
    @Override
    public Object eval(Object row, AstNode root) {
        Object leftValue = leftOperand.eval(row, root);
        Object rightValue = rightOperand.eval(row, root);
        if(leftValue instanceof Integer) {
            leftValue = (Double) leftValue;
        }
        if(rightValue instanceof Integer) {
            rightValue = (Double) rightValue;
        }  
        if(leftValue.getClass() != rightValue.getClass()) {
            throw new IllegalArgumentException("Left and right operand must be of the same type");
        }
        switch (operator) {
            case "+":
                if (leftValue instanceof Double && rightValue instanceof Double) {
                    return (Double) leftValue + (Double) rightValue;
                } else if(leftValue instanceof Duration && rightValue instanceof Duration) {
                    return ((Duration) leftValue).plus((Duration) rightValue);
                } else if (leftValue instanceof String || rightValue instanceof String) {
                    return String.valueOf(leftValue) + String.valueOf(rightValue);
                } else throw new IllegalArgumentException("Unsupported operand types for +: " + leftValue.getClass() + " and " + rightValue.getClass());
            case "-":
                if (leftValue instanceof Double && rightValue instanceof Double) {
                    return (Double) leftValue - (Double) rightValue;
                } else if(leftValue instanceof Duration && rightValue instanceof Duration) {
                    return ((Duration) leftValue).minus((Duration) rightValue);
                } else throw new IllegalArgumentException("Unsupported operand types for -: " + leftValue.getClass() + " and " + rightValue.getClass());
            case "*":
                if (leftValue instanceof Double && rightValue instanceof Double) {
                    return (Double) leftValue * (Double) rightValue;
                } else throw new IllegalArgumentException("Unsupported operand types for *: " + leftValue.getClass() + " and " + rightValue.getClass());
            case "/":
                if (leftValue instanceof Double && rightValue instanceof Double) {
                    if((Double) rightValue == 0.0) {
                        throw new ArithmeticException("Division by zero");   
                    }
                    return (Double) leftValue / (Double) rightValue;
                } else throw new IllegalArgumentException("Unsupported operand types for /: " + leftValue.getClass() + " and " + rightValue.getClass());
            case "%":
                if (leftValue instanceof Double && rightValue instanceof Double) {
                    if((Double) rightValue == 0.0) {
                        throw new ArithmeticException("Division by zero");
                    }
                    return (Double) leftValue % (Double) rightValue;
                } else throw new IllegalArgumentException("Unsupported operand types for %: " + leftValue.getClass() + " and " + rightValue.getClass());
            case "^":
                if (leftValue instanceof Double && rightValue instanceof Double) {
                    return Math.pow((Double) leftValue, (Double) rightValue);
                } else throw new IllegalArgumentException("Unsupported operand types for ^: " + leftValue.getClass() + " and " + rightValue.getClass());
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
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
