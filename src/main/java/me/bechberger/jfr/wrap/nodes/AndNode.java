package me.bechberger.jfr.wrap.nodes;

/*
 * AndNode represents a logical AND operation between two AST nodes.
 * It evaluates to true if both left and right nodes evaluate to true.
 * It evaluates the right side lazily
 */

public class AndNode extends AstConditional {
    private AstNode left;
    private AstNode right;

    public AndNode(AstNode left, AstNode right) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("Left and right nodes cannot be null");
        }
        this.left = left;
        this.right = right;
    }

    public AndNode() {}

    public void setLeft(AstNode left) {
        if (left == null) {
            throw new IllegalArgumentException("Left node cannot be null");
        }
        this.left = left;
    }

    public void setRight(AstNode right) {
        if (right == null) {
            throw new IllegalArgumentException("Right node cannot be null");
        }
        this.right = right;
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        sb.append("\n").append(dent).append("  Left: ").append(left.toString(indent + 1));
        sb.append("\n").append(dent).append("  Right: ").append(right.toString(indent + 1));
        return sb.toString();
    }

    /*
     * Findaggregates is a stage during the query execution plan in which aggregate functions are identified
     * Aggregate functions may be used with the AndNode within the Having clause
     */
    @Override
    public void findAggregates(AstNode root) {
        if (left != null) {
            left.findAggregates(root);
        }
        if (right != null) {
            right.findAggregates(root);
        }
    }   

    @Override
    public void evalNonAggregates(AstNode root) {
        if(left != null) {
            left.evalNonAggregates(root);
        }
        if(right != null) {
            right.evalNonAggregates(root);
        }
    }


    /*
     * When this Node is evaluated during either the Where or Having stage, it will evaluate the left side first.
     * If the left side evaluates to false, it will not evaluate the right side.
     * This is known as short-circuit evaluation.
     * If both sides evaluate to true, it will return true.
     * It is expected that both sides are of type boolean, as the underlying ConditionNode only does comparisons
     */
    @Override
    public Object eval(Object row, AstNode root) {
        if (left == null || right == null) {
            throw new IllegalStateException("Left and right nodes must be set before evaluation");
        }
        Boolean leftValue = (Boolean) left.eval(row, root);
        if(!leftValue){ 
            return false;
        }   
        Object rightValue = (Boolean) right.eval(row, root);
        
        if (leftValue instanceof Boolean && rightValue instanceof Boolean) {
            return (Boolean) leftValue && (Boolean) rightValue;
        } else {
            throw new IllegalArgumentException("Both left and right nodes must evaluate to Boolean values");
        }
    }
    
}
