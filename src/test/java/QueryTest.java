import org.junit.jupiter.api.Test;

import me.bechberger.jfr.wrap.Evaluator;
import me.bechberger.jfr.wrap.Lexer;
import me.bechberger.jfr.wrap.Parser;

public class QueryTest {
    // The voronoi2.jfr file is used for testing the query functionality
    // It is a smaller JFR file for which the queries can be evaluated quickly

    @Test
    public void testQuery() {
        Evaluator evaluator = Evaluator.getInstance();
        evaluator.setFile("src/test/resources/voronoi2.jfr");
        String query = "@SELECT * FROM [SELECT * FROM GCPhaseParallel] AS gcP GROUP BY gcP.eventThread, gcP.name HAVING COUNT() > 1 ORDER BY SUM(gcP.duration) LIMIT 6";
        Lexer lexer = new Lexer(query);
        Parser parser = new Parser(lexer.tokenize(), query);
        try {
            parser.parse().eval();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Query evaluation failed: " + e.getMessage());
        }
    }
}
