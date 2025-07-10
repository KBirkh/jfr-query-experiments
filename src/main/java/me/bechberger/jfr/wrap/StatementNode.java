package me.bechberger.jfr.wrap;

public class StatementNode extends AstNode {
    private AssignmentNode assignment;
    private ViewDefinitionNode viewDefinition;
    private QueryNode query;
    public boolean isAssignment = false;
    public boolean isViewDefinition = false;
    public boolean isQuery = false;

    public void addAssignment(AssignmentNode assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment cannot be null");
        }
        this.assignment = assignment;
        this.isAssignment = true;
    }

    public void addViewDefinition(ViewDefinitionNode viewDefinition) {
        if (viewDefinition == null) {
            throw new IllegalArgumentException("View definition cannot be null");
        }
        this.viewDefinition = viewDefinition;
        this.isViewDefinition = true;
    }

    public void addQuery(QueryNode query) {
        if (query == null) {
            throw new IllegalArgumentException("Query cannot be null");
        }
        this.query = query;
        this.isQuery = true;
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        if (isAssignment) {
            sb.append(assignment.toString(indent + 1));
        } else if (isViewDefinition) {
            sb.append(viewDefinition.toString(indent + 1));
        } else if (isQuery) {
            sb.append(query.toString(indent + 1));
        }
        return sb.toString();
    }

}
