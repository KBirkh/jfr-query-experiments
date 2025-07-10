package me.bechberger.jfr.wrap;

import java.util.List;

public class FromNode extends AstNode {
    private List<SourceNode> sources;

    public void addSource(SourceNode source) {
        if (sources == null) {
            sources = new java.util.ArrayList<>();
        }
        sources.add(source);
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        if (sources != null) {
            for (SourceNode source : sources) {
                sb.append(source.toString(indent + 1));
            }
        }
        return sb.toString();
    }

}
