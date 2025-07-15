package me.bechberger.jfr.wrap;

public class GCCorrelationNode extends AstNode { 
    private String identifier;
    private String correlation;

    public void setIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        this.identifier = identifier;
    }
    public void setCorrelation(String correlation) {
        if (correlation == null || correlation.isEmpty()) {
            throw new IllegalArgumentException("Correlation cannot be null or empty");
        }
        this.correlation = correlation;
    }
    public String getIdentifier() {
        return identifier;
    }
    public String getCorrelation() {
        return correlation;
    }
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        sb.append("\n").append(dent).append("  Identifier: ").append(identifier);
        sb.append("\n").append(dent).append("  Correlation: ").append(correlation);
        return sb.toString();
    }
    
}
