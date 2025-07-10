package me.bechberger.jfr.wrap;

public class SelectNode extends AstNode {
    private SelectListNode selectList;

    public void setSelectList(SelectListNode selectList) {
        if (selectList == null) {
            throw new IllegalArgumentException("Select list cannot be null");
        }
        this.selectList = selectList;
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        if (selectList != null) {
            sb.append(selectList.toString(indent + 1));
        } else {
            sb.append(dent).append("  <no select list>");
        }
        return sb.toString();
    }

}
