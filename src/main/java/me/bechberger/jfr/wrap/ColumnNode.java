package me.bechberger.jfr.wrap;

public class ColumnNode {
    private String name;
    private ColumnNode columnTail;

    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Column name cannot be null or empty");
        }
        this.name = name;
    }

    public void setTail(ColumnNode columnTail) {
        this.columnTail = columnTail;
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(": ").append(name);
        if (columnTail != null) {
            sb.append(",\n").append(columnTail.toString(indent + 1));
        }
        return sb.toString();

    }

}
