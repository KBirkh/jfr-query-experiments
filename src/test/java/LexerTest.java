import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import me.bechberger.jfr.wrap.*;

public class LexerTest {
    
    @Test
    public void testLexer() {
        String testString = "x = SELECT * FROM events WHERE condition == 4 ORDER BY identifier GROUP BY identifier";
        Lexer lexer = new Lexer(testString);
        List<Token> tokens = lexer.tokenize();
        assertNotNull(tokens);
        assertFalse(tokens.isEmpty());
        assertEquals(15, tokens.size());
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).type);
        assertEquals(TokenType.ASSIGNMENT, tokens.get(1).type);
        assertEquals(TokenType.SELECT, tokens.get(2).type);
        assertEquals(TokenType.ORDER_BY, tokens.get(10).type);
        assertEquals(TokenType.GROUP_BY, tokens.get(12).type);

    }
}
