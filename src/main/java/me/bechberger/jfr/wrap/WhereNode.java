package me.bechberger.jfr.wrap;

public class WhereNode extends AstNode {
    private ConditionNode condition;
    private ConditionNode conditionTail;

    public void setTail(ConditionNode conditionTail) {
        this.conditionTail = conditionTail;
    }

    public void setCondition(ConditionNode condition) {
        if (this.condition == null) {
            this.condition = condition;
        } else {
            throw new IllegalStateException("WhereNode can only have one condition");
        }
    }

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
