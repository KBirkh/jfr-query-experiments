package me.bechberger.jfr.wrap;

public class HavingNode extends AstNode {
    private ConditionNode condition;
    private ConditionNode conditionTail;

    public void setCondition(ConditionNode condition) {
        if (this.condition == null) {
            this.condition = condition;
        } else {
            throw new IllegalStateException("HavingNode can only have one condition");
        }
    }

    public void setTail(ConditionNode conditionTail) {
        if (this.conditionTail == null) {
            this.conditionTail = conditionTail;
        } else {
            throw new IllegalStateException("HavingNode can only have one condition tail");
        }
    }
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append(dent).append(this.getClass().getSimpleName()).append(":\n");
        if (condition != null) {
            sb.append(condition.toString(indent + 1)).append("\n");
        }
        if (conditionTail != null) {
            sb.append(conditionTail.toString(indent + 1)).append("\n");
        }
        return sb.toString();
    }

}
