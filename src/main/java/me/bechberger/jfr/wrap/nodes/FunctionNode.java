package me.bechberger.jfr.wrap.nodes;

import java.util.ArrayList;
import java.util.List;

import me.bechberger.jfr.wrap.EvalRow;

public class FunctionNode extends AstConditional {
    private String name;
    private List<AstNode> arguments;


    public FunctionNode(String name, List<AstNode> arguments) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be null or empty");
        }
        this.name = name;
        this.arguments = arguments;
    }

    public FunctionNode() {
        arguments = new ArrayList<AstNode>();
    }

    public FunctionNode(String name, AstNode... arguments) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be null or empty");
        }
        this.name = name;
        this.arguments = new ArrayList<>();
        for (AstNode arg : arguments) {
            if (arg == null) {
                throw new IllegalArgumentException("Function argument cannot be null");
            }
            this.arguments.add(arg);
        }
    }

    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be null or empty");
        }
        this.name = name;
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(": ").append(name);
        if (arguments != null && !arguments.isEmpty()) {
            sb.append("\n").append(dent).append("  Arguments:");
            for (AstNode arg : arguments) {
                sb.append(arg.toString(indent + 2));
            }
        } else {
            sb.append("\n").append(dent).append("  No arguments");
        }
        return sb.toString();
    }

    public void addArgument(AstNode expression) {
        arguments.add(expression);
    }

    public Object eval(EvalRow row) {
        return null; // TODO
    }
}
