package me.bechberger.jfr.wrap;

public class ProgramNode extends AstNode {
    private StatementNode statement;
    private ProgramNode tail;

    public ProgramNode() {

    }

    public void addStatement(StatementNode statement) {
        if (this.statement == null) {
            this.statement = statement;
        } else {
            throw new IllegalStateException("ProgramNode can only have one statement");
        }
    }

    public void addTail(ProgramNode tail) {
        if (this.tail == null) {
            this.tail = tail;
        } else {
            throw new IllegalStateException("ProgramNode can only have one tail");
        }
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append(dent).append(this.getClass().getSimpleName()).append(":");
        if (statement != null) {
            sb.append(statement.toString(indent + 1));
        }
        if (tail != null) {
            sb.append(tail.toString(indent + 1));
        }
        return sb.toString();
    }

}
