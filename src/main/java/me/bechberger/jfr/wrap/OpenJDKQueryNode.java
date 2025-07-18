package me.bechberger.jfr.wrap;

import me.bechberger.jfr.query.*;
import me.bechberger.jfr.tool.*;
import me.bechberger.jfr.util.UserDataException;
import me.bechberger.jfr.util.UserSyntaxException;

public class OpenJDKQueryNode extends AstNode {
    private String query;
    private int end;

    public OpenJDKQueryNode(String input, int start) {
        StringBuilder sb = new StringBuilder();
        int i = start;
        int toConsume = 0;
        while(i < input.length() && (input.charAt(i) != ';' || input.substring(i,i+2).equals("\n\n"))) {
            if(input.charAt(i) == '[') {
                toConsume++;
                if(toConsume == 1) {
                    i++;
                    continue;
                }
            } else if(input.charAt(i) == ']') {
                toConsume--;
                if(toConsume == 0) {
                    break;
                }
                
            }
            sb.append(input.charAt(i));
            i++;
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
    @Override
    public Table eval() {
        QueryCommand queryCommand = new QueryCommand();
        queryCommand.setView(query);
        queryCommand.setFile("src/main/java/me/bechberger/jfr/voronoi2.jfr");
        queryCommand.setConfigOptions(new ConfigOptions());
        try {
            return queryCommand.call();
        } catch (UserSyntaxException | UserDataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
}
