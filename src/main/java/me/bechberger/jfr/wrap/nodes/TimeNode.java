package me.bechberger.jfr.wrap.nodes;

import java.time.Duration;

/*
 * Represents a duration of time e.g. 10ms, 1ns, ...
 * Saves the timeValue as a double
 * and the Unit in String representation
 */
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

    /*
     * Provides a value by which to multiply
     * timeValue based on the Unit
     * the Baseunit used are nanoseconds
     */
    private long getUnitMultiplier() {
        switch (timeUnit.toLowerCase()) {
            case "ns":
                return 1L;
            case "µs":
                return 1_000L;
            case "us":
                return 1_000L;
            case "ms":
                return 1_000_000L;
            case "s":
                return 1_000_000_000L;
            case "m":
                return 60_000_000_000L;
            case "h":
                return 3_600_000_000_000L;
            default:
                throw new IllegalArgumentException("Unknown time unit: " + timeUnit);
        }
    }

    /*
     * Returns a Duration object
     */
    @Override
    public Object eval(Object row, AstNode root) {
        return Duration.ofNanos((long) timeValue * getUnitMultiplier());
    }

    /*
     * Leaf of conditional clauses and is not an aggregate
     * -> noop
     */
    @Override
    public void findAggregates(AstNode root) {
        // No aggregates to find in TimeNode
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(": ");
        sb.append(timeValue).append(" ").append(timeUnit);
        return sb.toString();
    }
}
