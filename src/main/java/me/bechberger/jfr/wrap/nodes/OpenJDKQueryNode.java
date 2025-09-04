package me.bechberger.jfr.wrap.nodes;

import me.bechberger.jfr.query.*;
import me.bechberger.jfr.tool.*;
import me.bechberger.jfr.util.UserDataException;
import me.bechberger.jfr.util.UserSyntaxException;
import me.bechberger.jfr.wrap.EvalTable;
import me.bechberger.jfr.wrap.Evaluator;
import me.bechberger.jfr.wrap.TableUtils;

/*
 * Represents a subquery which is evaluated by the
 * OpenJDK-provided querying tool
 */
public class OpenJDKQueryNode extends AstNode {
    private String query;
    private int end;

    /*
     * Constructs an OpenJDKQueryNode from a string input
     * The input String is the whole original query and 
     * the start int is the index at which the OpenJDK Query begins.
     * Removes leading and following Brackets.
     * Stops when a char which acts as EOQ is found or when all Brackets are matched
     */
    public OpenJDKQueryNode(String input, int start) {
        StringBuilder sb = new StringBuilder();
        int i = start;
        int toConsume = 0;
        while(i < input.length() && input.charAt(i) != ';') {
            if(i + 2 < input.length() && input.substring(i,i+2).equals("\n\n")) break;
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

    /*
     * Calls the OpenJDK tool to evaluate the query
     * If the query runs correctly convert the resulting table
     * to another data structure which will be used in further
     * evaluation
     */
    @Override
    public Object eval(Object aliasObj, AstNode root) {
        String alias = (String) aliasObj;
        Evaluator evaluator = Evaluator.getInstance();
        QueryCommand queryCommand = new QueryCommand();
        queryCommand.setView(query);
        queryCommand.setFile(evaluator.getFile());
        queryCommand.setConfigOptions(new ConfigOptions());
        try {
            Table table = queryCommand.call();
            if(table == null) {
                System.err.println("Query returned null table: " + query);
                return null;
            }
            EvalTable evalTable = TableUtils.toEvalTable(table, alias);
            evaluator.addToTable(evalTable, root);
        } catch (UserSyntaxException | UserDataException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object eval(AstNode root) {
        Evaluator evaluator = Evaluator.getInstance();
        QueryCommand queryCommand = new QueryCommand();
        queryCommand.setView(query);
        queryCommand.setFile(evaluator.getFile());
        queryCommand.setConfigOptions(new ConfigOptions());
        try {
            Table table = queryCommand.call();
            if(table == null) {
                throw new UserDataException("Query returned null table: " + query);
            }
            EvalTable evalTable = TableUtils.toEvalTable(table);
            evaluator.addToTable(evalTable, root);
        } catch (UserSyntaxException | UserDataException e) {
            System.err.println("Error executing OpenJDK query: " + e.getMessage());
            System.exit(1);
        }

        this.isEvaluated = true;
        return null;
    }
    
}
