package me.bechberger.jfr.wrap;

import java.util.*;

public class Lexer {
    private final String input;
    private int pos = 0;
    private final int length;

    private static final Map<String, TokenType> keywords = Map.ofEntries(
        Map.entry("SELECT", TokenType.SELECT),
        Map.entry("FROM", TokenType.FROM),
        Map.entry("WHERE", TokenType.WHERE),
        Map.entry("JOIN", TokenType.JOIN),
        Map.entry("ON", TokenType.ON),
        Map.entry("GROUP", TokenType.GROUP_BY),   // Handle "GROUP BY"
        Map.entry("BY", TokenType.GROUP_BY),      // merged later
        Map.entry("ORDER", TokenType.ORDER_BY),
        Map.entry("ASC", TokenType.ASC),
        Map.entry("DESC", TokenType.DESC),
        Map.entry("LIMIT", TokenType.LIMIT),
        Map.entry("VIEW", TokenType.VIEW),
        Map.entry("AS", TokenType.AS),
        Map.entry("TRUE", TokenType.BOOLEAN),
        Map.entry("FALSE", TokenType.BOOLEAN),
        Map.entry("COLUMN", TokenType.COLUMN),
        Map.entry("FORMAT", TokenType.FORMAT),
        Map.entry("HAVING", TokenType.HAVING),
        Map.entry("P90", TokenType.FUNCTION),
        Map.entry("P95", TokenType.FUNCTION),
        Map.entry("P99", TokenType.FUNCTION),
        Map.entry("P999", TokenType.FUNCTION),
        Map.entry("before_gc", TokenType.FIELD),
        Map.entry("after_gc", TokenType.FIELD),
        Map.entry("in", TokenType.IN),
        // Map.entry("minTimeSlice", TokenType.MIN_TIME_SLICE),
        Map.entry("LIKE", TokenType.LIKE),
        Map.entry("IN", TokenType.IN_OPERATOR),
        Map.entry("AND", TokenType.AND),
        Map.entry("OR", TokenType.OR),
        Map.entry("NOT", TokenType.NOT),
        Map.entry("MIN", TokenType.FUNCTION),
        Map.entry("MAX", TokenType.FUNCTION),
        Map.entry("COUNT", TokenType.FUNCTION),
        Map.entry("AVG", TokenType.FUNCTION),
        Map.entry("SUM", TokenType.FUNCTION)
    );

    public Lexer(String input) {
        this.input = input;
        this.length = input.length();
    }

    private char peek() {
        return pos < length ? input.charAt(pos) : '\0';
    }

    private char advance() {
        return input.charAt(pos++);
    }

    private boolean match(char expected) {
        if (peek() == expected) {
            pos++;
            return true;
        }
        return false;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (pos < length) {
            char c = peek();
            if (Character.isWhitespace(c)) {
                pos++;
                continue;
            }

            if (Character.isLetter(c)) {
                tokens.add(readWord());
            } else if (Character.isDigit(c)) {
                tokens.add(readNumber());
            } else {
                switch (c) {
                    case '=':
                        pos++;
                        if (match('=')) tokens.add(new Token(TokenType.EE, "=="));
                        else tokens.add(new Token(TokenType.ASSIGNMENT, "="));
                        break;
                    case '!':
                        pos++;
                        if (match('=')) tokens.add(new Token(TokenType.NEQ, "!="));
                        else throw new RuntimeException("Unexpected character: !");
                        break;
                    case '<':
                        pos++;
                        if (match('=')) tokens.add(new Token(TokenType.LE, "<="));
                        else tokens.add(new Token(TokenType.LT, "<"));
                        break;
                    case '>':
                        pos++;
                        if (match('=')) tokens.add(new Token(TokenType.GE, ">="));
                        else tokens.add(new Token(TokenType.GT, ">"));
                        break;
                    case '+':
                        tokens.add(new Token(TokenType.PLUS, String.valueOf(advance())));
                        break;
                    case '-':
                        tokens.add(new Token(TokenType.MINUS, String.valueOf(advance())));
                        break;
                    case '*':
                        tokens.add(new Token(TokenType.MULT, String.valueOf(advance())));
                        break;
                    case '/':
                        tokens.add(new Token(TokenType.DIV, String.valueOf(advance())));
                        break;
                    case '(':
                        tokens.add(new Token(TokenType.LPAREN, String.valueOf(advance())));
                        break;
                    case ')':
                        tokens.add(new Token(TokenType.RPAREN, String.valueOf(advance())));
                        break;
                    case ',':
                        tokens.add(new Token(TokenType.COMMA, String.valueOf(advance())));
                        break;
                    case ';':
                        tokens.add(new Token(TokenType.SEMICOLON, String.valueOf(advance())));
                        break;
                    case '@':
                        tokens.add(new Token(TokenType.AT, String.valueOf(advance())));
                        break;
                    case '[':
                        tokens.add(new Token(TokenType.LSPAREN, String.valueOf(advance())));
                        break;
                    case ']':
                        tokens.add(new Token(TokenType.RSPAREN, String.valueOf(advance())));
                        break;
                    case '^':
                        tokens.add(new Token(TokenType.EXP, String.valueOf(advance())));
                        break;
                    case '.':
                        tokens.add(new Token(TokenType.DOT, String.valueOf(advance())));
                        break; 
                    default:   
                        throw new RuntimeException("Unexpected character: " + c);
                }
            }
        }
        tokens.add(new Token(TokenType.EOF, ""));
        return mergeGroupAndOrderBy(tokens);
    }

    private Token readWord() {
        int start = pos;
        while (Character.isLetterOrDigit(peek()) || peek() == '_') advance();
        String word = input.substring(start, pos).toUpperCase();
        return new Token(keywords.getOrDefault(word, TokenType.IDENTIFIER), word);
    }

    private Token readNumber() {
        int start = pos;
        while (Character.isDigit(peek())) advance();
        if (peek() == '.') {
            advance();
            while (Character.isDigit(peek())) advance();
        }
        return new Token(TokenType.NUMBER, input.substring(start, pos));
    }

    // Combine "GROUP BY" and "ORDER BY"
    private List<Token> mergeGroupAndOrderBy(List<Token> tokens) {
        List<Token> merged = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            if (i + 1 < tokens.size()) {
                if (tokens.get(i).type == TokenType.GROUP_BY && tokens.get(i + 1).lexeme.equals("BY")) {
                    merged.add(new Token(TokenType.GROUP_BY, "GROUP BY"));
                    i++;
                    continue;
                }
                if (tokens.get(i).type == TokenType.ORDER_BY && tokens.get(i + 1).lexeme.equals("BY")) {
                    merged.add(new Token(TokenType.ORDER_BY, "ORDER BY"));
                    i++;
                    continue;
                }
            }
            merged.add(tokens.get(i));
        }
        return merged;
    }

}
