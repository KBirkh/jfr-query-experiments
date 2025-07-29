package me.bechberger.jfr.wrap.nodes;

import me.bechberger.jfr.wrap.EvalRow;
import me.bechberger.jfr.wrap.Evaluator;

public class IdentifierNode extends AstConditional {
    private String identifier;
    private String tableAlias;
    private boolean hasTableAlias;
    private String type;

    public IdentifierNode(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        this.identifier = identifier;
    }

    public IdentifierNode(String identifier, String tableAlias) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        this.identifier = identifier;
        this.tableAlias = tableAlias;
        this.hasTableAlias = tableAlias != null && !tableAlias.isEmpty();
    } 

    public IdentifierNode() {

    }

    public void setIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        this.identifier = identifier;
    }

    public void setTableAlias(String tableAlias) {
        if (tableAlias == null || tableAlias.isEmpty()) {
            throw new IllegalArgumentException("Table alias cannot be null or empty");
        }
        this.tableAlias = tableAlias;
        this.hasTableAlias = true;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getTableAlias() {
        return tableAlias;
    } 

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(": ").append(identifier);
        if(type != null) {
            sb.append(" : ").append(type);
        }
        if (tableAlias != null && !tableAlias.isEmpty()) {
            sb.append("\n").append(dent).append("  ").append("Table: ").append(tableAlias);
        }
        return sb.toString();
    }

    public boolean compareTo(IdentifierNode identifier) {
        if(this.getIdentifier() == identifier.getIdentifier()) {
            if(this.hasTableAlias == identifier.hasTableAlias) {
                if(this.hasTableAlias) {
                    if(this.getTableAlias() == identifier.getTableAlias()) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }
        this.type = type;
    }

    @Override
    public Object eval(Object row, AstNode root) {
        EvalRow evalrow = (EvalRow) row;
        if (row == null) {
            throw new IllegalArgumentException("EvalRow cannot be null");
        }
        String toSearch = hasTableAlias ? tableAlias + "_" + identifier : identifier;
        Object value = evalrow.getFields().get(toSearch);
        if (value == null) {
            Evaluator evaluator = Evaluator.getInstance();
            value = evaluator.getAssignment(this);
        }
        return value;
    }

    @Override
    public String getName() {
        return hasTableAlias ? tableAlias + "." + identifier : identifier;
    }
    
}
