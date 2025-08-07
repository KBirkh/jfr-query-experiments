package me.bechberger.jfr.wrap.nodes;

import java.util.List;

/*
 * Represents a FROM clause in the abstract syntax tree.
 * It contains a list of sources (tables or subqueries) from which data is selected.
 */
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

    /*
     * During evaluation of this node there can be a special case
     * where the tables to be selected form may not exist yet
     * In those cases it returns -1 and puts the root node as a ToDo
     */
    @Override
    public Object eval(AstNode root) {
        for(AstNode source : sources) {
            if(((Integer) source.eval(root)) == -1) {
                return -1; // Indicating that this is a source node to be evaluated later
            }
        }
        return 0;
    }

}
