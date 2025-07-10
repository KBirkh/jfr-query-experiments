package me.bechberger.jfr.wrap;

public class LimitNode {
    private int count;

    public void setCount(String count) {
        try {
            this.count = Integer.parseInt(count);
            if (this.count < 0) {
                throw new IllegalArgumentException("Count cannot be negative");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Count must be a valid integer", e);
        }
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append(dent).append(this.getClass().getSimpleName()).append(": ").append(count);
        return sb.toString();
    }

}
