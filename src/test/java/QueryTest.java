import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import me.bechberger.jfr.wrap.Column;
import me.bechberger.jfr.wrap.EvalRow;
import me.bechberger.jfr.wrap.EvalTable;
import me.bechberger.jfr.wrap.Evaluator;
import me.bechberger.jfr.wrap.Lexer;
import me.bechberger.jfr.wrap.Parser;

public class QueryTest {
    // The voronoi2.jfr file is used for testing the query functionality
    // It is a smaller JFR file for which the queries can be evaluated quickly
    public final String filepath = "src/test/resources/voronoi2.jfr";


    @Test
    public void testQuery() throws ParseException {
        Evaluator evaluator = Evaluator.getInstance();
        evaluator.setFile(filepath);
        String query = "@SELECT * FROM [SELECT * FROM GCPhaseParallel] AS gcP GROUP BY gcP.eventThread, gcP.name HAVING COUNT() > 1 ORDER BY SUM(gcP.duration) LIMIT 6";
        Lexer lexer = new Lexer(query);
        Parser parser = new Parser(lexer.tokenize(), query);
        parser.parse().eval();
        evaluator.destruct();
    }

    @Test
    public void simpleTest() throws ParseException {
        Evaluator evaluator = Evaluator.getInstance();
        evaluator.setFile(filepath);
        String query = "@SELECT COUNT() FROM [SELECT * FROM GCPhaseParallel] GROUP BY eventThread LIMIT 1";
        Lexer lexer = new Lexer(query);
        Parser parser = new Parser(lexer.tokenize(), query);
        parser.parse().eval();

        List<Column> columns = new ArrayList<>();
        columns.add(new Column("COUNT()", null));
        List<EvalRow> rows = new ArrayList<>();
        EvalRow row = new EvalRow();
        row.addField("COUNT()", "59");
        rows.add(row);
        EvalTable expected = new EvalTable(columns, rows);
        String result = evaluator.getOutput();
        assertEquals(expected.toString(), result, "Expected and actual results do not match");
        evaluator.destruct();
    }

    @Test
    public void testRowCount() throws ParseException {
        Evaluator evaluator = Evaluator.getInstance();
        evaluator.setFile(filepath);
        String query = "@SELECT eventThread FROM [SELECT * FROM GCPhaseParallel] GROUP BY eventThread";
        Lexer lexer = new Lexer(query);
        Parser parser = new Parser(lexer.tokenize(), query);
        parser.parse().eval();
    
        // Expecting 4 unique eventThreads (example, adjust as needed)
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("eventThread", null));
        List<EvalRow> rows = new ArrayList<>();
        rows.add(new EvalRow().addField("eventThread", "GC Thread#2"));
        rows.add(new EvalRow().addField("eventThread", "GC Thread#3"));
        rows.add(new EvalRow().addField("eventThread", "GC Thread#5"));
        rows.add(new EvalRow().addField("eventThread", "GC Thread#0"));
        rows.add(new EvalRow().addField("eventThread", "GC Thread#1"));
        rows.add(new EvalRow().addField("eventThread", "GC Thread#4"));
        rows.add(new EvalRow().addField("eventThread", "VM Thread"));
        rows.add(new EvalRow().addField("eventThread", "GC Thread#6"));
        EvalTable expected = new EvalTable(columns, rows);
    
        String result = evaluator.getOutput();
        assertEquals(expected.toString(), result, "Row count or eventThread values do not match");
        evaluator.destruct();
    }
    
    @Test
    public void testOrderDeterminism() throws ParseException {
        Evaluator evaluator = Evaluator.getInstance();
        evaluator.setFile(filepath);
        String query = "@SELECT eventThread FROM [SELECT * FROM GCPhaseParallel] GROUP BY eventThread ORDER BY eventThread";
        Lexer lexer = new Lexer(query);
        Parser parser = new Parser(lexer.tokenize(), query);
        parser.parse().eval();
    
        // The order should be deterministic (alphabetical)
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("eventThread", null));
        List<EvalRow> rows = new ArrayList<>();
        rows.add(new EvalRow().addField("eventThread", "GC Thread#0"));
        rows.add(new EvalRow().addField("eventThread", "GC Thread#1"));
        rows.add(new EvalRow().addField("eventThread", "GC Thread#2"));
        rows.add(new EvalRow().addField("eventThread", "GC Thread#3"));
        rows.add(new EvalRow().addField("eventThread", "GC Thread#4"));
        rows.add(new EvalRow().addField("eventThread", "GC Thread#5"));
        rows.add(new EvalRow().addField("eventThread", "GC Thread#6"));
        rows.add(new EvalRow().addField("eventThread", "VM Thread"));
        EvalTable expected = new EvalTable(columns, rows);
    
        String result = evaluator.getOutput();
        assertEquals(expected.toString(), result, "Order of eventThread values is not deterministic");
        evaluator.destruct();
    }
    
    @Test
    public void testSumAggregate() throws ParseException {
        Evaluator evaluator = Evaluator.getInstance();
        evaluator.setFile(filepath);
        String query = "@SELECT SUM(duration) FROM [SELECT * FROM GCPhaseParallel]";
        Lexer lexer = new Lexer(query);
        Parser parser = new Parser(lexer.tokenize(), query);
        parser.parse().eval();
    
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("SUM(duration)", null));
        List<EvalRow> rows = new ArrayList<>();
        // Replace "123456789" with the actual expected sum for your test file
        rows.add(new EvalRow().addField("SUM(duration)", Duration.ofNanos(9846999)));
        EvalTable expected = new EvalTable(columns, rows);
    
        String result = evaluator.getOutput();
        assertEquals(expected.toString(), result, "SUM aggregate result does not match expected value");
        evaluator.destruct();
    }

    @Test
    public void testCountAggregate() throws ParseException {
        Evaluator evaluator = Evaluator.getInstance();
        evaluator.setFile(filepath);
        String query = "@SELECT COUNT() FROM [SELECT * FROM GCPhaseParallel]";
        Lexer lexer = new Lexer(query);
        Parser parser = new Parser(lexer.tokenize(), query);
        parser.parse().eval();
    
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("COUNT()", null));
        List<EvalRow> rows = new ArrayList<>();
        rows.add(new EvalRow().addField("COUNT()", "395")); // Adjust based on your data
        EvalTable expected = new EvalTable(columns, rows);
    
        String result = evaluator.getOutput();
        assertEquals(expected.toString(), result, "COUNT aggregate result does not match expected value");
        evaluator.destruct();
    }

    @Test
    public void testGroupByWithHaving() throws ParseException {
        Evaluator evaluator = Evaluator.getInstance();
        evaluator.setFile(filepath);
        String query = "@SELECT eventThread, COUNT() FROM [SELECT * FROM GCPhaseParallel] GROUP BY eventThread HAVING COUNT() > 1 ORDER BY eventThread";
        Lexer lexer = new Lexer(query);
        Parser parser = new Parser(lexer.tokenize(), query);
        parser.parse().eval();
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("eventThread", null));
        columns.add(new Column("COUNT()", null));
        List<EvalRow> rows = new ArrayList<>();
        rows.add(new EvalRow().addField("eventThread", "GC Thread#0").addField("COUNT()", "58"));
        rows.add(new EvalRow().addField("eventThread", "GC Thread#1").addField("COUNT()", "49"));
        rows.add(new EvalRow().addField("eventThread", "GC Thread#2").addField("COUNT()", "59"));
        rows.add(new EvalRow().addField("eventThread", "GC Thread#3").addField("COUNT()", "56"));
        rows.add(new EvalRow().addField("eventThread", "GC Thread#4").addField("COUNT()", "53"));
        rows.add(new EvalRow().addField("eventThread", "GC Thread#5").addField("COUNT()", "59"));
        rows.add(new EvalRow().addField("eventThread", "GC Thread#6").addField("COUNT()", "7"));
        rows.add(new EvalRow().addField("eventThread", "VM Thread").addField("COUNT()", "54"));
        EvalTable expected = new EvalTable(columns, rows);
        assertEquals(expected.toString(), evaluator.getOutput(), "Group by with having result does not match expected value");
        evaluator.destruct();
    }
}
