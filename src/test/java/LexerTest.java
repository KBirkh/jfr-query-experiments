import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import me.bechberger.jfr.wrap.*;

public class LexerTest {
    
    @Test
    public void testLexer() {
        Lexer lexer = new Lexer("x = SELECT * FROM events WHERE condition == 4 ORDER BY identifier GROUP BY identifier");
        List<Token> tokens = lexer.tokenize();
        assertNotNull(tokens);
        assertFalse(tokens.isEmpty());
        assertEquals(15, tokens.size());
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).type);
        assertEquals(TokenType.ASSIGNMENT, tokens.get(1).type);
        assertEquals(TokenType.SELECT, tokens.get(2).type);
        assertEquals(TokenType.ORDER_BY, tokens.get(10).type);
        assertEquals(TokenType.GROUP_BY, tokens.get(12).type);
        lexer = new Lexer("SELECT col+2, SUM(col, col) FROM table, tablo WHERE 12 == +11 AND lipstick.beforeGC != p99(1+1) OR meme < meiemi; x=SELECT * FROM events");
        tokens = lexer.tokenize();
        assertNotNull(tokens);
        assertFalse(tokens.isEmpty());
        assertEquals(43, tokens.size());
        assertEquals(TokenType.PLUS, tokens.get(2).type);
        assertEquals(TokenType.COMMA, tokens.get(4).type);
        assertEquals(TokenType.FUNCTION, tokens.get(5).type);
        assertEquals(TokenType.COMMA, tokens.get(8).type);
    }
}
