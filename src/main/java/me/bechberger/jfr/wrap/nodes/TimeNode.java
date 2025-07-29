package me.bechberger.jfr.wrap.nodes;

import java.time.Duration;

import me.bechberger.jfr.wrap.EvalRow;

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

    @Override
    public Object eval(Object row) {
        return Duration.ofNanos((long) timeValue * getUnitMultiplier());
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(": ");
        sb.append(timeValue).append(" ").append(timeUnit);
        return sb.toString();
    }
}
