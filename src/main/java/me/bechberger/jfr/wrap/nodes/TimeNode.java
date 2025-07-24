package me.bechberger.jfr.wrap.nodes;

public class TimeNode extends AstConditional {

    private double timeValue;
    private String timeUnit;

    public TimeNode(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input cannot be null or empty");
        }
        for(int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isDigit(c) || c == '.' || c == '-') {
                continue;
            } else {
                timeUnit = input.substring(i).trim();
                timeValue = Double.parseDouble(input.substring(0, i).trim());
                return;
            }
        }

    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(": ");
        sb.append(timeValue).append(" ").append(timeUnit);
        return sb.toString();
    }
}
