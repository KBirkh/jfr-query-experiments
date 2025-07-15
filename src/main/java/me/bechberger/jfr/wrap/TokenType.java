package me.bechberger.jfr.wrap;

public enum TokenType {
    // Keywords
    SELECT, FROM, WHERE, JOIN, ON, GROUP_BY, ORDER_BY, ASC, DESC, LIMIT, VIEW, AS, COLUMN, FORMAT, HAVING,
    P90, P95, P99, P999, BEFORE_GC, AFTER_GC, IN, MIN_TIME_SLICE, LIKE, IN_OPERATOR, AND, OR, NOT,

    // Operators
    ASSIGNMENT, EE, NEQ, LT, LE, GT, GE, PLUS, MINUS, MULT, DIV, EXP,

    // Symbols
    LPAREN, RPAREN, COMMA, SEMICOLON, AT, STAR, LSPAREN, RSPAREN, DOT,

    // Literals
    IDENTIFIER, NUMBER, TEXT, FUNCTION, FIELD, BOOLEAN, STRING, 

    // End of query
    EOQ,
    
    // End of file
    EOF
}