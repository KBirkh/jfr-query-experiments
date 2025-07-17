package me.bechberger.jfr.wrap;

public class Token {
    public final TokenType type;
    public final String lexeme;
    public final int pos;

    public Token(TokenType type, String lexeme, int pos) {
        this.type = type;
        this.lexeme = lexeme;
        this.pos = pos;
    }

    @Override
    public String toString() {
        return type + ":" + pos + "('" + lexeme + "')";
    }
}
