package me.bechberger.jfr.wrap;

public class WhereNode extends AstNode {
    private AstNode condition;
    private ConditionNode conditionTail;

    public WhereNode() {

    }

    public WhereNode(AstNode condition) {
        if (condition == null) {
            throw new IllegalArgumentException("Condition cannot be null");
        }
        this.condition = condition;
    }

    public void setTail(ConditionNode conditionTail) {
        this.conditionTail = conditionTail;
    }

    public void setCondition(AstNode condition) {
        if (this.condition == null) {
            this.condition = condition;
        } else {
            throw new IllegalStateException("WhereNode can only have one condition");
        }
    }
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        if (condition != null) {
            sb.append(condition.toString(indent + 1));
        }
        if (conditionTail != null) {
            sb.append(conditionTail.toString(indent + 1));
        }
        return sb.toString();
    }

}
