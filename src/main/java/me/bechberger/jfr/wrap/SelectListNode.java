package me.bechberger.jfr.wrap;

public class SelectListNode {
    private SelectListNode selectList;
    private ExpressionNode expression;
    public boolean isStar;

    public void setSelectList(SelectListNode selectList) {
        this.selectList = selectList;
        this.isStar = false;
    }

    public void setExpression(ExpressionNode expression) {
        if (expression == null) {
            throw new IllegalArgumentException("Expression cannot be null");
        }
        this.expression = expression;
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        if (isStar) {
            sb.append(" *");
        } else {
            if (selectList != null) {
                sb.append(selectList.toString(indent + 1));
            }
            if (expression != null) {
                sb.append(expression.toString(indent + 1));
            }
        }
        return sb.toString();
    }

}
