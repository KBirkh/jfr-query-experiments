package me.bechberger.jfr.wrap.nodes;

import java.util.List;

public class FromNode extends AstNode {
    private List<AstNode> sources;

    public FromNode() {
        sources = new java.util.ArrayList<>();
    }

    public FromNode(List<AstNode> sources) {
        this.sources = sources;
    }

    public FromNode(AstNode... sources) {
        this.sources = new java.util.ArrayList<>();
        for (AstNode source : sources) {
            this.sources.add(source);
        }
    }

    public void addSource(AstNode source) {
        if (sources == null) {
            sources = new java.util.ArrayList<>();
        }
        sources.add(source);
    }
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        if (sources != null) {
            for (AstNode source : sources) {
                sb.append(source.toString(indent + 1));
            }
        }
        return sb.toString();
    }

    @Override
    public void eval() {
        for(AstNode source : sources) {
            source.eval();
        }
    }

}
