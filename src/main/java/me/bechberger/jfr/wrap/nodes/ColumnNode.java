package me.bechberger.jfr.wrap.nodes;

public class ColumnNode extends AstNode {
    private String name;
    private ColumnNode columnTail;

    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Column name cannot be null or empty");
        }
        this.name = name;
    }

    public ColumnNode() {
        // Default constructor
    }

    public ColumnNode(String name, ColumnNode columnTail) {
        setName(name);
        setTail(columnTail);
    }

    public ColumnNode(String name) {
        setName(name);
        this.columnTail = null; // Initialize tail to null if not provided
    }

    public void setTail(ColumnNode columnTail) {
        this.columnTail = columnTail;
    }
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(": ").append(name);
        if (columnTail != null) {
            sb.append(",\n").append(columnTail.toString(indent + 1));
        }
        return sb.toString();

    }

    public void addColumn(String lexeme) {
        if(name == null || name.isEmpty()) {
            setName(lexeme);
        } else {
            if (columnTail == null) {
                columnTail = new ColumnNode(lexeme);
            } else {
                columnTail.addColumn(lexeme);
            }
        }
    }

}
