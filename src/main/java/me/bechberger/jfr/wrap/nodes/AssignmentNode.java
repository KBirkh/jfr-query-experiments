package me.bechberger.jfr.wrap.nodes;

public class AssignmentNode extends AstNode {
    private AstNode identifier;
    private AstNode node;

    public void setIdentifier(AstNode identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("Identifier cannot be null");
        }
        this.identifier = identifier;
    }

    public AssignmentNode() {

    }

    public AssignmentNode(AstNode identifier, AstNode expression) {
        setIdentifier(identifier);
        setNode(expression);
    }

    public void setNode(AstNode node) {
        if (node == null) {
            throw new IllegalArgumentException("Query cannot be null");
        }
        this.node = node;
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        sb.append("\n").append(dent).append("  Identifier: ").append(identifier.toString(indent + 1));
        if (node != null) {
            sb.append(node.toString(indent + 1));
        }
        return sb.toString();
    }

    @Override
    public Object eval(Object row, AstNode root) {
        return node.eval(row, root);
    }

}
