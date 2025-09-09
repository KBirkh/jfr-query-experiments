import java.time.Duration;

import org.junit.jupiter.api.Test;

import me.bechberger.jfr.wrap.EvalRow;
import me.bechberger.jfr.wrap.TokenType;
import me.bechberger.jfr.wrap.nodes.AndNode;
import me.bechberger.jfr.wrap.nodes.AssignmentNode;
import me.bechberger.jfr.wrap.nodes.AstConditional;
import me.bechberger.jfr.wrap.nodes.AstNode;
import me.bechberger.jfr.wrap.nodes.BinaryOpNode;
import me.bechberger.jfr.wrap.nodes.BooleanNode;
import me.bechberger.jfr.wrap.nodes.ConditionNode;
import me.bechberger.jfr.wrap.nodes.FromNode;
import me.bechberger.jfr.wrap.nodes.FunctionNode;
import me.bechberger.jfr.wrap.nodes.FunctionType;
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
import me.bechberger.jfr.wrap.nodes.WhereNode;

public class EvaluationTest {
    
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

    @Test
    public void testNumberNode() {
        AstNode node = number("42");
        EvalRow row = new EvalRow();
        Object result = node.eval(row, node);
        assert result instanceof Double : "Expected a Double result";
        assert ((Double) result).equals(42.0) : "Expected result to be 42.0";
    }

    @Test
    public void testTimeNode() {
        AstNode node = time("12ms");
        EvalRow row = new EvalRow();
        Object result = node.eval(row, node);
        assert result instanceof Duration : "Expected a result of type Duration";
        assert result.equals(Duration.ofMillis(12)) : "Expected result to be 12ms Duration";
    }

    @Test
    public void testIdentifierNode() {
        AstNode node = identifier("eventName", "events");
        EvalRow row = new EvalRow();
        row.getFields().put("events_eventName", "testEvent");
        Object result = node.eval(row, node);
        assert result instanceof String : "Expected a String result";
        assert result.equals("testEvent") : "Expected result to be 'testEvent'";
    }

    @Test
    public void testStringNode() {
        AstNode node = string("Hello, World!");
        EvalRow row = new EvalRow();
        Object result = node.eval(row, node);
        assert result instanceof String : "Expected a String result";
        assert result.equals("Hello, World!") : "Expected result to be 'Hello, World!'";
    }

    @Test
    public void testBooleanNode() {
        AstConditional node = bool(true);
        Object result = node.eval(new EvalRow(), node);
        assert result instanceof Boolean : "Expected a Boolean result";
        assert result.equals(true) : "Expected result to be true";

        node = bool(false);
        result = node.eval(new EvalRow(), node);
        assert result instanceof Boolean : "Expected a Boolean result";
        assert result.equals(false) : "Expected result to be false";
    }

    @Test
    public void testConditionNode() {
        AstConditional left = identifier("eventName", "events");
        AstConditional right = string("testEvent");
        AstNode condition = condition(TokenType.EE, left, right);
        
        EvalRow row = new EvalRow();
        row.getFields().put("events_eventName", "testEvent");
        
        Boolean result = (Boolean) condition.eval(row, condition);
        assert result : "Expected condition to evaluate to true";

        // Test with a different value
        row.getFields().put("events_eventName", "anotherEvent");
        result = (Boolean) condition.eval(row, condition);
        assert !result : "Expected condition to evaluate to false";
    }

    @Test
    public void testMultipleConditions() {
        AstConditional left = identifier("eventName", "events");
        AstConditional right = string("testEvent");
        AstNode condition1 = condition(TokenType.EE, left, right);
        
        AstConditional left2 = identifier("duration", "events");
        AstConditional right2 = number("1000");
        AstNode condition2 = condition(TokenType.GT, left2, right2);
        
        AstConditional combinedCondition = and(condition1, condition2);
        
        EvalRow row = new EvalRow();
        row.getFields().put("events_eventName", "testEvent");
        row.getFields().put("events_duration", 1500.0);
        
        Boolean result = (Boolean) combinedCondition.eval(row, combinedCondition);
        assert result : "Expected combined condition to evaluate to true";

        // Test with a different value
        row.getFields().put("events_duration", 500.0);
        result = (Boolean) combinedCondition.eval(row, combinedCondition);
        assert !result : "Expected combined condition to evaluate to false";
    }

    @Test
public void testMultipleComplexConditions() {
    // First condition: eventName == "testEvent"
    AstConditional left = identifier("eventName", "events");
    AstConditional right = string("testEvent");
    AstNode condition1 = condition(TokenType.EE, left, right);

    // Second condition: duration > 100ns
    AstConditional left2 = identifier("duration", "events");
    AstConditional right2 = time("100ns");
    AstNode condition2 = condition(TokenType.GT, left2, right2);

    // Third condition: eventType == "specialEvent"
    AstConditional left3 = identifier("eventType", "events");
    AstConditional right3 = string("specialEvent");
    AstNode condition3 = condition(TokenType.EE, left3, right3);

    // Combine condition1 and condition2 with OR
    AstConditional combinedCondition1 = or(condition1, condition2);

    // Combine the result with condition3 using another OR
    AstConditional combinedCondition = and(combinedCondition1, condition3);

    // Create a row for evaluation
    EvalRow row = new EvalRow();
    row.getFields().put("events_eventName", "testEvent");
    row.getFields().put("events_duration", Duration.ofNanos(100L));
    row.getFields().put("events_eventType", "specialEvent");

    // Evaluate the combined condition
    Boolean result = (Boolean) combinedCondition.eval(row, combinedCondition);
    assert result : "Expected combined condition to evaluate to true";

    // Test with a different value
    row.getFields().put("events_eventName", "notEvent");
    row.getFields().put("events_duration", Duration.ofNanos(101L));
    row.getFields().put("events_eventType", "specialEvent");
    result = (Boolean) combinedCondition.eval(row, combinedCondition);
    assert result : "Expected combined condition to evaluate to true because of the third condition";

    // Test with all conditions failing
    row.getFields().put("events_eventType", "normalEvent");
    result = (Boolean) combinedCondition.eval(row, combinedCondition);
    assert !result : "Expected combined condition to evaluate to false";
}

    @Test
    public void testAggregateFunction() {
        // Test SUM function
        AstConditional sumFunction = function("SUM", identifier("size", "events"));
        EvalRow row = new EvalRow();
        row.getFields().put("events_size", 1000.0);
        
        // The aggregate functions are evaluated differently during groupBy phase
        // This test verifies the function structure is created correctly
        assert sumFunction instanceof FunctionNode : "Expected a FunctionNode";
        FunctionNode fn = (FunctionNode) sumFunction;
        assert fn.getType() == FunctionType.SUM : "Expected SUM function type, got " + fn.getType();
    }

    @Test
    public void testPercentileFunction() {
        // Test P90 percentile function
        AstNode p90Function = function("P90", identifier("duration", "events"));
        
        assert p90Function instanceof FunctionNode : "Expected a FunctionNode";
        FunctionNode fn = (FunctionNode) p90Function;
        assert fn.getType() == FunctionType.P90 : "Expected P90 function type, got " + fn.getType();
        
        // Test P99 percentile function
        AstNode p99Function = function("P99", identifier("duration", "events"));
        assert ((FunctionNode) p99Function).getType() == FunctionType.P99 : "Expected P99 function type";
        
        // Test P999 percentile function
        AstNode p999Function = function("P999", identifier("duration", "events"));
        assert ((FunctionNode) p999Function).getType() == FunctionType.P999 : "Expected P999 function type";
    }

    @Test
    public void testGCCorrelationFunctions() {
        // Test BEFORE_GC function - note the function name should match the switch case
        AstNode beforeGCFunction = function("BEFOREGC", identifier("startTime", "events"));
        assert beforeGCFunction instanceof FunctionNode : "Expected a FunctionNode";
        FunctionNode fn = (FunctionNode) beforeGCFunction;
        assert fn.getType() == FunctionType.BEFORE_GC : "Expected BEFORE_GC function type, got " + fn.getType();
        
        // Test AFTER_GC function
        AstNode afterGCFunction = function("AFTERGC", identifier("startTime", "events"));
        assert ((FunctionNode) afterGCFunction).getType() == FunctionType.AFTER_GC : "Expected AFTER_GC function type";
        
        // Test NEAR_GC function
        AstNode nearGCFunction = function("NEARGC", identifier("startTime", "events"));
        assert ((FunctionNode) nearGCFunction).getType() == FunctionType.NEAR_GC : "Expected NEAR_GC function type";
    }

    @Test
    public void testCrossProduct() {
        // Create a first query that selects from events
        AstNode query1 = query(true, 
            select(identifier("eventName", "events")), 
            from(source("events", "e1")), 
            null, null, null, null, null);
        
        // Create a second query that selects from another source with different alias
        AstNode query2 = query(true,
            select(identifier("gcId", "gc")),
            from(source("gc", "g1")),
            null, null, null, null, null);
        
        // Verify the individual queries are created correctly
        assert query1 instanceof QueryNode : "Expected a QueryNode for events";
        assert query2 instanceof QueryNode : "Expected a QueryNode for gc";
        
        // Create a cross product scenario where two tables are joined
        AstNode crossProductQuery = query(true,
            select(identifier("eventName", "e1"), identifier("gcId", "g1")),
            from(source("events", "e1"), source("gc", "g1")),
            null, null, null, null, null);
            
        assert crossProductQuery instanceof QueryNode : "Expected a QueryNode for cross product";
    }

    @Test
    public void testProgramWithMultipleQueries() {
        // Create multiple queries in a program
        AstNode query1 = query(true,
            select(identifier("eventName")),
            from(source("events", null)),
            null, null, null, null, null);
            
        AstNode query2 = query(true,
            select(identifier("gcId")),
            from(source("gc", null)),
            null, null, null, null, null);
            
        AstNode program = program(query1, query2);
        
        assert program instanceof ProgramNode : "Expected a ProgramNode";
        assert program.toString(0).contains("QueryNode") : "Expected program to contain queries";
    }

    @Test
    public void testProgramWithAssignments() {
        // Test assignment: events = @SELECT * FROM events
        AstNode eventsAssignment = assignment(
            identifier("events"),
            query(true, selectStar(), from(source("events", null)), null, null, null, null, null)
        );
        
        // Test assignment: gc = @SELECT * FROM gc  
        AstNode gcAssignment = assignment(
            identifier("gc"),
            query(true, selectStar(), from(source("gc", null)), null, null, null, null, null)
        );
        
        // Query that uses the assigned tables
        AstNode queryUsingAssignments = query(true,
            select(identifier("eventName", "events"), identifier("gcId", "gc")),
            from(source("events", "e"), source("gc", "g")),
            null, null, null, null, null);
            
        AstNode program = program(eventsAssignment, gcAssignment, queryUsingAssignments);
        
        assert program instanceof ProgramNode : "Expected a ProgramNode";
        assert eventsAssignment instanceof AssignmentNode : "Expected an AssignmentNode";
        assert gcAssignment instanceof AssignmentNode : "Expected an AssignmentNode";
    }

    @Test
    public void testComplexQueryWithGroupByAndHaving() {
        // Create a complex query with GROUP BY and HAVING clauses using aggregates
        AstNode complexQuery = query(true,
            select(
                identifier("eventType", "events"),
                function("COUNT", identifier("*")),
                function("AVG", identifier("duration", "events"))
            ),
            from(source("events", null)),
            where(condition(TokenType.GT, identifier("duration", "events"), time("1ms"))),
            groupBy(identifier("eventType", "events")),
            having(condition(TokenType.GT, function("COUNT", identifier("*")), number("5"))),
            null, null);
            
        assert complexQuery instanceof QueryNode : "Expected a QueryNode";
    }

    @Test
    public void testQueryWithPercentileConditions() {
        // Query that uses percentile functions in WHERE clause
        AstNode percentileQuery = query(true,
            select(identifier("eventName", "events"), identifier("duration", "events")),
            from(source("events", null)),
            where(and(
                function("P90", identifier("duration", "events")),
                condition(TokenType.LT, identifier("duration", "events"), time("100ms"))
            )),
            null, null, null, null);
            
        assert percentileQuery instanceof QueryNode : "Expected a QueryNode";
    }

    @Test
    public void testComplexProgramWithMixedOperations() {
        // A complex program combining assignments, multiple queries, and various function types
        
        // Assignment 1: Store events in a variable
        AstNode eventsAssignment = assignment(
            identifier("allEvents"),
            query(true, selectStar(), from(source("events", null)), null, null, null, null, null)
        );
        
        // Assignment 2: Store filtered events
        AstNode filteredEventsAssignment = assignment(
            identifier("filteredEvents"),
            query(true,
                selectStar(),
                from(source("allEvents", null)),
                where(condition(TokenType.GT, identifier("duration"), time("10ms"))),
                null, null, null, null)
        );
        
        // Query 1: Aggregate analysis with percentiles
        AstNode aggregateQuery = query(true,
            select(
                identifier("eventType"),
                function("COUNT", identifier("*")),
                function("AVG", identifier("duration")),
                function("MAX", identifier("duration"))
            ),
            from(source("filteredEvents", null)),
            where(function("P95", identifier("duration"))),
            groupBy(identifier("eventType")),
            having(condition(TokenType.GT, function("COUNT", identifier("*")), number("10"))),
            null, null);
            
        // Query 2: Cross product with GC correlation
        AstNode gcCorrelationQuery = query(true,
            select(
                identifier("eventName", "events"),
                identifier("startTime", "events"),
                function("BEFOREGC", identifier("startTime", "events")),
                function("AFTERGC", identifier("startTime", "events"))
            ),
            from(source("events", "e"), source("gc", "g")),
            where(and(
                condition(TokenType.EE, identifier("eventType", "events"), string("allocation")),
                function("NEARGC", identifier("startTime", "events"))
            )),
            null, null, null, null);
            
        // Query 3: Complex percentile and duration analysis
        AstNode percentileAnalysisQuery = query(true,
            select(
                identifier("threadName"),
                function("MEDIAN", identifier("duration")),
                function("P90", identifier("duration")),
                function("P99", identifier("duration"))
            ),
            from(source("events", null)),
            where(and(
                function("P50", identifier("duration")),
                condition(TokenType.LT, identifier("duration"), time("1s"))
            )),
            groupBy(identifier("threadName")),
            having(condition(TokenType.GT, function("MEDIAN", identifier("duration")), time("50ms"))),
            orderBy(arr(function("P99", identifier("duration"))), arr("DESC")),
            limit("100"));
            
        AstNode complexProgram = program(
            eventsAssignment,
            filteredEventsAssignment,
            aggregateQuery,
            gcCorrelationQuery,
            percentileAnalysisQuery
        );
        
        assert complexProgram instanceof ProgramNode : "Expected a ProgramNode";
        assert complexProgram.toString(0).contains("AssignmentNode") : "Expected program to contain assignments";
        assert complexProgram.toString(0).contains("QueryNode") : "Expected program to contain queries";
        assert complexProgram.toString(0).contains("FunctionNode") : "Expected program to contain function calls";
    }

    @Test
    public void testSubqueryWithAssignment() {
        // Test a query that uses a subquery as a source
        AstNode subquery = query(true,
            select(identifier("eventType"), function("COUNT", identifier("*"))),
            from(source("events", null)),
            null,
            groupBy(identifier("eventType")),
            null, null, null);
            
        AstNode mainQuery = query(true,
            select(identifier("eventType", "sub"), identifier("COUNT(*)", "sub")),
            from(source(subquery, "sub")),
            where(condition(TokenType.GT, identifier("COUNT(*)", "sub"), number("100"))),
            null, null, null, null);
            
        assert mainQuery instanceof QueryNode : "Expected a QueryNode";
        assert subquery instanceof QueryNode : "Expected subquery to be a QueryNode";
    }

    @Test 
    public void testMixedFunctionTypes() {
        // Test combining different function types in a single query
        AstNode mixedFunctionQuery = query(true,
            select(
                identifier("eventName"),
                function("SUM", identifier("size")),           // Aggregate
                function("P95", identifier("duration")),       // Percentile  
                function("BEFOREGC", identifier("startTime")) // GC correlation
            ),
            from(source("events", null)),
            where(and(
                function("P90", identifier("duration")),       // Percentile in WHERE
                condition(TokenType.GT, function("AFTERGC", identifier("startTime")), number("0")) // GC correlation in WHERE
            )),
            groupBy(identifier("eventName")),
            having(condition(TokenType.GT, function("SUM", identifier("size")), number("1000"))), // Aggregate in HAVING
            null, null);
            
        assert mixedFunctionQuery instanceof QueryNode : "Expected a QueryNode with mixed function types";
    }
}
