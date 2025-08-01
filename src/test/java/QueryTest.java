import java.text.ParseException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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
    }

    @Test
    public void simpleTest() throws ParseException {
        Evaluator evaluator = Evaluator.getInstance();
        evaluator.setFile(filepath);
        String query = "@SELECT COUNT() FROM [SELECT * FROM GCPhaseParallel] GROUP BY eventThread LIMIT 1";
        Lexer lexer = new Lexer(query);
        Parser parser = new Parser(lexer.tokenize(), query);
        parser.parse().eval();

        String result = evaluator.toString();
        assertEquals(result, "Rows:\n{59}\n\nColumns: eventThread, COUNT(), ");
    }
}
