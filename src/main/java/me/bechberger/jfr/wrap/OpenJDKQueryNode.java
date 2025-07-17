package me.bechberger.jfr.wrap;

public class OpenJDKQueryNode extends AstNode {
    private String query;
    private int end;

    public OpenJDKQueryNode(String input, int start) {
        StringBuilder sb = new StringBuilder();
        int i = start;
        int toConsume = 0;
        while (i < input.length() && (input.charAt(i) != ';' || input.substring(i,i+2).equals("\n\n"))) {
            if(input.charAt(i) != '[' && input.charAt(i) != ']' || toConsume > 0) {
                sb.append(input.charAt(i));
            } 
            if(input.charAt(i) == '[') toConsume++;
            else if(input.charAt(i) == ']') toConsume--;
            i++;
        }
        if(toConsume != 0) {
            throw new IllegalArgumentException("Unmatched brackets in query starting at index " + start);
        }
        end = i;
        query = sb.toString();
    }

    public String getQuery() {
        return query;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(": ").append(query);
        return sb.toString();
    }
    
}
