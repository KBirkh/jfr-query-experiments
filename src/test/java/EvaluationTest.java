import java.time.Duration;
import java.time.temporal.TemporalUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import me.bechberger.jfr.wrap.EvalRow;
import me.bechberger.jfr.wrap.TokenType;
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

}
