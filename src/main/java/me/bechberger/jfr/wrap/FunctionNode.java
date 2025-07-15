package me.bechberger.jfr.wrap;

public class FunctionNode extends AstNode {
    private String name;
    private AstNode argument;

    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be null or empty");
        }
        this.name = name;
    }

    public void setArgument(AstNode argument) {
        if (argument == null) {
            throw new IllegalArgumentException("Argument cannot be null");
        }
        this.argument = argument;
    }
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append(dent).append(this.getClass().getSimpleName()).append(": ").append(name)
          .append(" ").append(argument).append("\n");
        return sb.toString();
    }
}
