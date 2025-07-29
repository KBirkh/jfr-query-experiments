import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.util.stream.Stream;

import me.bechberger.jfr.wrap.*;
import me.bechberger.jfr.wrap.nodes.AndNode;
import me.bechberger.jfr.wrap.nodes.ArithmeticNode;
import me.bechberger.jfr.wrap.nodes.AssignmentNode;
import me.bechberger.jfr.wrap.nodes.AstConditional;
import me.bechberger.jfr.wrap.nodes.AstNode;
import me.bechberger.jfr.wrap.nodes.BinaryOpNode;
import me.bechberger.jfr.wrap.nodes.BooleanNode;
import me.bechberger.jfr.wrap.nodes.ColumnNode;
import me.bechberger.jfr.wrap.nodes.ConditionNode;
import me.bechberger.jfr.wrap.nodes.FromNode;
import me.bechberger.jfr.wrap.nodes.FunctionNode;
import me.bechberger.jfr.wrap.nodes.GroupByNode;
import me.bechberger.jfr.wrap.nodes.HavingNode;
import me.bechberger.jfr.wrap.nodes.IdentifierNode;
import me.bechberger.jfr.wrap.nodes.LimitNode;
import me.bechberger.jfr.wrap.nodes.NumberNode;
import me.bechberger.jfr.wrap.nodes.OpenJDKQueryNode;
import me.bechberger.jfr.wrap.nodes.OrNode;
import me.bechberger.jfr.wrap.nodes.OrderByNode;
import me.bechberger.jfr.wrap.nodes.ProgramNode;
import me.bechberger.jfr.wrap.nodes.QueryNode;
import me.bechberger.jfr.wrap.nodes.SelectNode;
import me.bechberger.jfr.wrap.nodes.SourceNode;
import me.bechberger.jfr.wrap.nodes.StringNode;
import me.bechberger.jfr.wrap.nodes.TimeNode;
import me.bechberger.jfr.wrap.nodes.UnaryOpNode;
import me.bechberger.jfr.wrap.nodes.ViewDefinitionNode;
import me.bechberger.jfr.wrap.nodes.WhereNode;

public class ParserTest {

    // Helper functions for tree building

    public AstNode program(AstNode... statements) {
        return new ProgramNode(statements);
    }

    public AstNode select(AstNode... columns) {
        return new SelectNode(columns);
    }

    public AstNode selectStar() {
        SelectNode selectNode = new SelectNode();
        selectNode.isStar = true; // Indicate that this is a SELECT *
        return selectNode;
    }

    public AstNode from(AstNode... columns) {
        return new FromNode(columns);
    }

    public AstNode where(AstNode condition) {
        return new WhereNode(condition);
    }

    public AstNode groupBy(AstNode... columns) {
        return new GroupByNode(columns);
    }

    public AstNode having(AstNode condition) {
        return new HavingNode(condition); 
    }

    public AstNode orderBy(AstNode[] columns, String[] directions) {
        return new OrderByNode(columns, directions);
    }

    public AstNode limit(String count) {
        return new LimitNode(count);
    }

    public AstConditional and(AstNode left, AstNode right) {
        return new AndNode(left, right);
    }

    public AstConditional arithmetic(String operator, AstNode left, AstNode right) {
        return new ArithmeticNode(left, operator, right);
    }

    public AstNode assignment(AstNode variable, AstNode value) {
        return new AssignmentNode(variable, value);
    }

    public AstNode openJDK(String query) {
        return new OpenJDKQueryNode(query, 0);
    }

    public AstConditional binaryOp(String operator, AstConditional left, AstConditional right) {
        return new BinaryOpNode(operator, left, right);
    }

    public AstConditional bool(boolean value) {
        return new BooleanNode(value);
    }

    public AstNode column(String... names) {
        ColumnNode col = new ColumnNode();
        for (String name : names) {
            col.addColumn(name);
        }
        return col;
    }

    public AstNode condition(TokenType operator, AstConditional left, AstConditional right) {
        return new ConditionNode(operator, left, right);
    }

    public AstConditional or(AstNode left, AstNode right) {
        return new OrNode(left, right);
    }
    
    public AstConditional function(String name, AstNode... arguments) {
        return new FunctionNode(name, arguments);
    }
    
    public AstNode query(boolean hasAt, AstNode select, AstNode from, AstNode where, AstNode groupBy, AstNode having, AstNode orderBy, AstNode limit) {
        QueryNode queryNode = new QueryNode(hasAt);
        queryNode.setSelect(select);
        queryNode.setFrom((FromNode) from);
        queryNode.setWhere((WhereNode) where);
        queryNode.setGroupBy((GroupByNode) groupBy);
        queryNode.setHaving((HavingNode) having);
        queryNode.setOrderBy((OrderByNode) orderBy);
        queryNode.setLimit((LimitNode) limit);
        return queryNode;
    }
    
    public SourceNode source(String name, String alias) {
        SourceNode sourceNode = new SourceNode(name);
        if (alias != null) {
            sourceNode.setAlias(alias);
        }
        return sourceNode;
    }
    
    public AstNode source(AstNode subquery, String alias) {
        SourceNode sourceNode = new SourceNode(subquery);
        if (alias != null) {
            sourceNode.setAlias(alias);
        }
        return sourceNode;
    }
    
    public AstNode viewDefinition(String name, AstNode query) {
        return new ViewDefinitionNode(name, query);
    }
    
    public AstConditional number(String value) {
        return new NumberNode(value);
    }

    public AstConditional time(String timeUnit) {
        return new TimeNode(timeUnit);
    }
    
    public AstConditional string(String value) {
        return new StringNode(value);
    }
    
    public AstConditional identifier(String name) {
        return new IdentifierNode(name);
    }
    
    public AstConditional identifier(String name, String table) {
        return new IdentifierNode(name, table);
    }

    public AstConditional unaryOp(String operator, AstConditional operand) {
        return new UnaryOpNode(operator, operand);
    }

    public String[] arr(String... elements) {
        return elements;
    }

    public AstNode[] arr(AstNode... elements) {
        return elements;
    }

    // Tests

    @Test
    public void testTreeBuilder() throws ParseException {
        String input = "@SELECT * FROM events";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        AstNode res = parser.parse();

        AstNode expected = program(query(true, selectStar(), from(source("events", null)), null, null, null, null, null));
        assertEquals(expected.toString(0), res.toString(0), "Parsed tree does not match expected structure");
    }

    @Test
    public void testArithmeticExpressionInSelect() throws ParseException {
        String input = "@SELECT col + 5 * 2 FROM table";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        AstNode res = parser.parse();

        // Expected tree: SELECT (col + (5 * 2)) FROM table
        AstNode expected = program(
            query(true, 
                select(binaryOp("+", identifier("col"), binaryOp("*", number("5"), number("2")))), 
                from(source("table", null)), null, null, null, null, null
            )
        );

        assertEquals(expected.toString(0), res.toString(0), "Parsed tree for arithmetic expression in SELECT does not match expected structure");
    }

    @Test
    public void testFunctionCallInSelect() throws ParseException {
        String input = "@SELECT SUM(col1, col2) FROM table";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        AstNode res = parser.parse();

        // Expected tree: SELECT SUM(col1, col2) FROM table
        AstNode expected = program(
            query(true, 
                select(function("SUM", identifier("col1"), identifier("col2"))), 
                from(source("table", null)), null, null, null, null, null
            )
        );

        assertEquals(expected.toString(0), res.toString(0), "Parsed tree for function call in SELECT does not match expected structure");
    }

    @Test
    public void testQueryAssignment() throws ParseException {
        String input = "x = @SELECT * FROM events";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        AstNode res = parser.parse();

        // Expected tree: x = SELECT * FROM events
        AstNode expected = program(
            assignment(identifier("x"), query(true, selectStar(), from(source("events", null)), null, null, null, null, null))
        );

        assertEquals(expected.toString(0), res.toString(0), "Parsed tree for query assignment does not match expected structure");
    }

    @Test
    public void testArithmeticExpressionInWhere() throws ParseException {
        String input = "@SELECT * FROM table WHERE col1 + 5 > col2 * 2";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        AstNode res = parser.parse();

        // Expected tree: WHERE (col1 + 5) > (col2 * 2)
        AstNode expected = program(
            query(true, 
                selectStar(), 
                from(source("table", null)), 
                where(condition(TokenType.GT, binaryOp("+", identifier("col1"), number("5")), binaryOp("*", identifier("col2"), number("2")))), 
                null, null, null, null
            )
        );

        assertEquals(expected.toString(0), res.toString(0), "Parsed tree for arithmetic expression in WHERE does not match expected structure");
    }

    @Test
    public void testFunctionCallInWhere() throws ParseException {
        String input = "@SELECT * FROM table WHERE SUM(col1, col2) > 100";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        AstNode res = parser.parse();

        // Expected tree: WHERE SUM(col1, col2) > 100
        AstNode expected = program(
            query(true, 
                selectStar(), 
                from(source("table", null)), 
                where(condition(TokenType.GT, function("SUM", identifier("col1"), identifier("col2")), number("100"))), 
                null, null, null, null
            )
        );

        assertEquals(expected.toString(0), res.toString(0), "Parsed tree for function call in WHERE does not match expected structure");
    }

    @Test
    public void testMissingSelectClause() {
        String input = "FROM table";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        assertThrows(ParseException.class, parser::parse, "Parser should throw an exception for missing SELECT clause");
    }

    @Test
    public void testMissingFromClause() {
        String input = "@SELECT column";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        assertThrows(ParseException.class, parser::parse, "Parser should throw an exception for missing FROM clause");
    }

    @Test
    public void testInvalidOperator() {
        String input = "@SELECT column FROM table WHERE column @ 10";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        assertThrows(ParseException.class, parser::parse, "Parser should throw an exception for invalid operator");
    }

    @Test
    public void testUnclosedParenthesis() {
        String input = "@SELECT column FROM [@SELECT column2 FROM table2";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        assertThrows(ParseException.class, parser::parse, "Parser should throw an exception for unclosed parenthesis");
    }

    @Test
    public void testInvalidFunctionCallSyntax() {
        String input = "@SELECT SUM(column1 column2) FROM table";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        assertThrows(ParseException.class, parser::parse, "Parser should throw an exception for invalid function call syntax");
    }

    @Test
    public void testInvalidQueryAssignment() {
        String input = "variable = @SELECT FROM events";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        assertThrows(ParseException.class, parser::parse, "Parser should throw an exception for invalid query assignment");
    }

    @Test
    public void testSimpleSelect() throws ParseException {
        String input = "@SELECT col FROM table";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        assertDoesNotThrow(parser::parse, "Parser should successfully parse a simple SELECT query");
    }

    @Test
    public void testSelectWithWhere() throws ParseException {
        String input = "@SELECT col FROM table WHERE col > 10";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        assertDoesNotThrow(parser::parse, "Parser should successfully parse a SELECT query with WHERE clause");
    }

    @Test
    public void testSelectWithSubquery() throws ParseException {
        String input = "@SELECT col FROM [@SELECT column2 FROM table2] AS subquery";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        assertDoesNotThrow(parser::parse, "Parser should successfully parse a SELECT query with subquery");
    }

    @Test
    public void testSelectWithArithmeticExpression() throws ParseException {
        String input = "@SELECT (col + 5) * 2 FROM table";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        assertDoesNotThrow(parser::parse, "Parser should successfully parse a SELECT query with arithmetic expression");
    }

    @Test
    public void testSelectWithFunctionCall() throws ParseException {
        String input = "@SELECT SUM(column1, column2) FROM table";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        assertDoesNotThrow(parser::parse, "Parser should successfully parse a SELECT query with function call");
    }

    @Test
    public void testMultipleQueries() throws ParseException {
        String input = "@SELECT col FROM table; @SELECT col2 FROM table2";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        AstNode res = parser.parse();

        // Expected tree: Program with two queries
        AstNode expected = program(
            query(true, select(identifier("col")), from(source("table", null)), null, null, null, null, null),
            query(true, select(identifier("col2")), from(source("table2", null)), null, null, null, null, null)
        );

        assertEquals(expected.toString(0), res.toString(0), "Parsed tree for multiple queries does not match expected structure");
    }

    @Test
    public void testMultipleQueries2() throws ParseException {
        String input = "@SELECT col FROM table; @SELECT col2 FROM table2\n\nx = @SELECT * FROM events";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        AstNode res = parser.parse();

        // Expected tree: Program with three queries
        AstNode expected = program(
            query(true, select(identifier("col")), from(source("table", null)), null, null, null, null, null),
            query(true, select(identifier("col2")), from(source("table2", null)), null, null, null, null, null),
            assignment(identifier("x"), query(true, selectStar(), from(source("events", null)), null, null, null, null, null))
        );

        assertEquals(expected.toString(0), res.toString(0), "Parsed tree for multiple queries with assignment does not match expected structure");
    }

    @Test
    public void testEverything() throws ParseException {
        String input = "@SELECT col1 + 5 * 2, SUM(col2) FROM table WHERE col3 > 10 GROUP BY col4 HAVING SUM(col5) < 100 ORDER BY col6 LIMIT 10; x = @SELECT * FROM events; SELECT * FROM events; @SELECT * FROM [SELECT * FROM events] AS ho";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        AstNode res = parser.parse();

        // Expected tree: Program with complex query and assignment
        AstNode expected = program(
            query(true, 
                select(
                    binaryOp("+", identifier("col1"), binaryOp("*", number("5"), number("2"))), 
                    function("SUM", identifier("col2"))
                ), 
                from(source("table", null)), 
                where(condition(TokenType.GT, identifier("col3"), number("10"))), 
                groupBy(identifier("col4")), 
                having(condition(TokenType.LT, function("SUM", identifier("col5")), number("100"))), 
                orderBy(arr(identifier("col6")), arr("ASC")), 
                limit("10")
            ),
            assignment(identifier("x"), query(true, selectStar(), from(source("events", null)), null, null, null, null, null)),
            openJDK("SELECT * FROM events"),
            query(true, selectStar(), from(source(openJDK("SELECT * FROM events"), "ho")), null, null, null, null, null)
        );

        assertEquals(expected.toString(0), res.toString(0), "Parsed tree for complex query with assignment does not match expected structure");
    }

    @Test
    public void testOpenJDK() throws ParseException {
        String input = "@SELECT * FROM [SELECT * FROM events]";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        AstNode res = parser.parse();

        // Expected tree

        AstNode expected = program(
                query(true,
                    selectStar(), from(source(openJDK("SELECT * FROM events"), null)), null, null, null, null, null
                )
            );

        assertEquals(expected.toString(0), res.toString(0), "Distinction between regular query and OpenJDK query made");
        
    }

    @Test
    public void testOpenJDK2() throws ParseException {
        String input = "SELECT * FROM events";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        AstNode res = parser.parse();

        AstNode expected = program(openJDK("SELECT * FROM events"));

        assertEquals(expected.toString(0), res.toString(0), "OpenJDK query is not recognized correctly");
    }

    @Test
    public void testTableAlias() throws ParseException {
        String input = "@SELECT * FROM table AS t WHERE t.col > 10";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        AstNode res = parser.parse();

        AstNode expected = program(
            query(true, 
                selectStar(), 
                from(source("table", "t")), 
                where(condition(TokenType.GT, identifier("col", "t"), number("10"))), 
                null, null, null, null
            )
        );
        assertEquals(expected.toString(0), res.toString(0), "Table alias in FROM clause is not parsed correctly");
    }

    private static Stream<String> provideQueries() {
        return Stream.of(
            // Simple SELECT query
            "@SELECT col1 FROM table",
            // SELECT with WHERE clause
            "@SELECT col1 FROM table WHERE col1 > 10",
            // SELECT with subquery
            "@SELECT col1 FROM [@SELECT col2 FROM table2] AS subquery",
            // SELECT with arithmetic expression
            "@SELECT (col1 + 5) * 2 FROM table",
            // SELECT with function call
            "@SELECT SUM(col1, col2) FROM table",
            // Assignment with OpenJDK query
            "x = SELECT * FROM events",
            // OpenJDK query
            "@SELECT * FROM [SELECT * FROM events]",
            // Complex query with multiple features
            "@SELECT col1 + 5 * 2, SUM(col2) FROM table as tab WHERE col3 > 10 GROUP BY tabcol4 HAVING SUM(tabcol5) < 100 ORDER BY col6 LIMIT 10"
        );
    }

    @ParameterizedTest
    @MethodSource("provideQueries")
    public void testCombinedQueries(String query) throws ParseException {
        Lexer lexer = new Lexer(query);
        Parser parser = new Parser(lexer.tokenize(), query);
        assertDoesNotThrow(parser::parse, "Parser should successfully parse the query: " + query);
    }

    @Test
    public void testTableIdentifiers() throws ParseException {
        String input = "@SELECT t.col1 FROM table AS t WHERE p99(t.col2) == id AND t.col3 < 100";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        AstNode res = parser.parse();

        AstNode expected = program(
            query(true, 
                select(identifier("col1", "t")), 
                from(source("table", "t")), 
                where(and(condition(TokenType.EE, function("p99", identifier("col2", "t")), identifier("id")), condition(TokenType.LT, identifier("col3", "t"), number("100")))), 
                null, null, null, null
            )
        );

        assertEquals(expected.toString(0), res.toString(0), "Parsed tree for table identifiers does not match expected structure");
    }

    @Test
    public void testAllTheThings() throws ParseException {
        String input = "@SELECT col1 + 5 * 2, SUM(col2) FROM table AS t WHERE t.col3 > 10 GROUP BY col4, t.col6 HAVING SUM(col5) < 100 ORDER BY col6 DESC LIMIT 10; x = @SELECT * FROM events; SELECT * FROM events; @SELECT * FROM [SELECT * FROM events] AS ho \n\n @SELECT * FROM [SELECT * FROM [SELECT * FROM events] AS t] AS p";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        AstNode res = parser.parse();

        AstNode expected = program(
            query(true, 
                select(
                    binaryOp("+", identifier("col1"), binaryOp("*", number("5"), number("2"))), 
                    function("SUM", identifier("col2"))
                ), 
                from(source("table", "t")), 
                where(condition(TokenType.GT, identifier("col3", "t"), number("10"))), 
                groupBy(identifier("col4"), identifier("col6", "t")), 
                having(condition(TokenType.LT, function("SUM", identifier("col5")), number("100"))), 
                orderBy(arr(identifier("col6")), arr("DESC")), 
                limit("10")
            ),
            assignment(identifier("x"), query(true, selectStar(), from(source("events", null)), null, null, null, null, null)),
            openJDK("SELECT * FROM events"),
            query(true, selectStar(), from(source(openJDK("[SELECT * FROM events]"), "ho")), null, null, null, null, null),
            query(true, selectStar(), from(source(openJDK("[SELECT * FROM [SELECT * FROM events] AS t]"), "p")), null, null, null, null, null)
        );
        assertEquals(expected.toString(0), res.toString(0), "Parsed tree for complex query with all features does not match expected structure");
    }

    @Test
    public void testTimeUnits() throws ParseException {
        String input = "@SELECT * FROM table WHERE duration > 5ms AND duration < 10s";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        AstNode res = parser.parse();

        AstNode expected = program(
            query(true, 
                selectStar(), 
                from(source("table", null)), 
                where(and(condition(TokenType.GT, identifier("duration"), time("5ms")), condition(TokenType.LT, identifier("duration"), time("10s")))), 
                null, null, null, null
            )
        );
        assertEquals(expected.toString(0), res.toString(0), "Parsed tree for time units in WHERE clause does not match expected structure");
    }

    @Test
    public void testSingleCondition() throws ParseException {
        String input = "@SELECT * FROM table WHERE p99(duration)";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        AstNode res = parser.parse();

        AstNode expected = program(
            query(true, 
                selectStar(), 
                from(source("table", null)), 
                where(function("p99", identifier("duration"))), 
                null, null, null, null
            )
        );
        assertEquals(expected.toString(0), res.toString(0), "Parsed tree for single condition in WHERE clause does not match expected structure");
    }



}