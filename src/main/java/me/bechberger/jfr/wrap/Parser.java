package me.bechberger.jfr.wrap;

import java.text.ParseException;
import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public AstNode parse() throws ParseException {
        return program();
    }

    private AstNode program() throws ParseException {
        if(isIn(TokenType.IDENTIFIER, TokenType.VIEW, TokenType.AT, TokenType.SELECT)) {
            return statementList();
        } else if(isIn(TokenType.EOF)) {
            return new ProgramNode(); // Empty program
        } else {
            throw new ParseException("Expected IDENTIFIER, VIEW, AT, SELECT, or EOF, found " + peek().type, pos);
        }
    }
    
    private AstNode statementList() throws ParseException {
        ProgramNode programNode = new ProgramNode();
        if(isIn(TokenType.EOF)) {
            return programNode;
        }
        else if(isIn(TokenType.IDENTIFIER, TokenType.VIEW, TokenType.AT, TokenType.SELECT)) {
            programNode.addStatement(statement());
            while(isIn(TokenType.EOQ)) {
                advance();
                programNode.addStatement(statement());
            }
            return programNode;
        }
        throw new ParseException("Expected IDENTIFIER, VIEW, AT, SELECT, or EOF, found " + peek().type, pos);
    }

    private AstNode statement() throws ParseException {
        if(isIn(TokenType.IDENTIFIER)) {
            return assignment();
        } else if(isIn(TokenType.VIEW)) {
            return viewDefinition();
        } else if(isIn(TokenType.SELECT, TokenType.AT)) {
            return query();
        } else {
            throw new ParseException("Expected IDENTIFIER, VIEW, AT, or SELECT, found " + peek().type, pos);
        }
    }

    private AstNode assignment() throws ParseException {
        AssignmentNode assignmentNode = new AssignmentNode();
        if(isIn(TokenType.IDENTIFIER)) {
            assignmentNode.setIdentifier(expect(TokenType.IDENTIFIER).lexeme);
            expect(TokenType.ASSIGNMENT);
            assignmentNode.setQuery(query());
            return assignmentNode;
        } else {
            throw new ParseException("Expected IDENTIFIER for assignment, found " + peek().type, pos);
        }
    }

    private AstNode viewDefinition() throws ParseException {
        ViewDefinitionNode viewDefinitionNode = new ViewDefinitionNode();
        if(isIn(TokenType.VIEW)) {
            advance();
            viewDefinitionNode.setName(expect(TokenType.IDENTIFIER).lexeme);
            expect(TokenType.AS);
            viewDefinitionNode.setQuery(query());
            return viewDefinitionNode;
        } else {
            throw new ParseException("Expected VIEW keyword, found " + peek().type, pos);
        }
    }

    private AstNode query() throws ParseException {
        QueryNode queryNode = new QueryNode();
        if(isIn(TokenType.SELECT, TokenType.AT)) {
            queryPrefix(queryNode);
            queryNode.setSelect(select());
            queryNode.setFrom(from());
            querySuffix(queryNode);
            return queryNode;
        } else {
            throw new ParseException("Expected SELECT or AT keyword, found " + peek().type, pos);
        }
    }

    private void queryPrefix(QueryNode queryNode) throws ParseException {
        if(isIn(TokenType.AT)) {
            queryNode.hasAt = true;
            advance();
        } else if(isIn(TokenType.COLUMN)) {
            queryNode.setColumn(column());
        } else if(isIn(TokenType.SELECT)) {
            return;
        } else {
            throw new ParseException("Expected AT, SELECT, or COLUMN, found " + peek().type, pos);
        }
    }

    private AstNode column() throws ParseException {
        ColumnNode columnNode = new ColumnNode();
        if(isIn(TokenType.COLUMN)) {
            advance();
            expect(TokenType.IDENTIFIER);
            columnNode.addColumn(expect(TokenType.IDENTIFIER).lexeme);
            while( isIn(TokenType.COMMA)) {
                advance();
                columnNode.addColumn(expect(TokenType.IDENTIFIER).lexeme);
            }
            return columnNode;
        } else {
            throw new ParseException("Expected COLUMN keyword, found " + peek().type, pos);
        }
    }

    private AstNode select() throws ParseException {
        if(isIn(TokenType.SELECT)) {
            advance();
            return selectList();
        } else {
            throw new ParseException("Expected SELECT keyword, found " + peek().type, pos);
        }
    }

    private AstNode selectList() throws ParseException {
        SelectNode selectNode = new SelectNode();
        if(isIn(TokenType.MULT)) {
            selectNode.isStar = true;
            advance();
            return selectNode;
        } else if(isIn(TokenType.FUNCTION, TokenType.FIELD, TokenType.NUMBER, TokenType.IDENTIFIER, TokenType.LPAREN, TokenType.LSPAREN, TokenType.PLUS, TokenType.MINUS)) {
            selectNode.addColumn(expression());
            while(isIn(TokenType.COMMA)) {
                advance();
                selectNode.addColumn(expression());
            }
            return selectNode;
        } else {
            throw new ParseException("Expected MULT, FUNCTION, FIELD, NUMBER, IDENTIFIER, LPAREN, LSPAREN, PLUS, or MINUS, found " + peek().type, pos);
        }
    }

    private AstNode expression() throws ParseException {
        if(isIn(TokenType.BOOLEAN)) {
            BooleanNode boolNode = new BooleanNode();
            boolNode.setValue(Boolean.parseBoolean(peek().lexeme));
            advance();
            return boolNode;
        } else if(isIn(TokenType.STRING)) {
            StringNode stringNode = new StringNode();
            stringNode.setValue(peek().lexeme);
            advance();
            return stringNode;
        } else if(isIn(TokenType.PLUS, TokenType.MINUS, TokenType.LPAREN, TokenType.IDENTIFIER, TokenType.NUMBER, TokenType.FUNCTION)) {
            AstNode arithmetic = arithmetic();
            if(isIn(TokenType.AS) && arithmetic instanceof ArithmeticNode) {
                ArithmeticNode arithmeticNode = (ArithmeticNode) arithmetic;
                alias(arithmeticNode);
            }
            return arithmetic;
        } else if(isIn(TokenType.LSPAREN)) {
            return query();
        } else {
            throw new ParseException("Expected FUNCTION, IDENTIFIER, BOOLEAN, STRING, PLUS, MINUS, LPAREN, NUMBER, LSPAREN, or AS, found " + peek().type, pos);
        }
    }

    private void alias(ArithmeticNode node) throws ParseException {
        advance();
        node.setAlias(expect(TokenType.IDENTIFIER).lexeme);
    }

    private FromNode from() throws ParseException {
        FromNode fromNode = new FromNode();
        if(isIn(TokenType.FROM)) {
            advance();
            source(fromNode);
            fromTail(fromNode);
            return fromNode;
        } else {
            throw new ParseException("Expected FROM keyword, found " + peek().type, pos);
        }
    }

    private void fromTail(FromNode node) throws ParseException {
        while(isIn(TokenType.COMMA)) {
            advance();
            source(node);
        }
        if(isIn(TokenType.WHERE, TokenType.GROUP_BY, TokenType.ORDER_BY, TokenType.LIMIT, TokenType.EOQ, TokenType.EOF)) {
            return;
        } else {
            throw new ParseException("Expected COMMA, WHERE, GROUP, ORDER, LIMIT, or EOF after source, found " + peek().type, pos);
        }
    }

    private void source(FromNode node) throws ParseException {
        if(isIn(TokenType.IDENTIFIER)) {
            SourceNode sourceNode = new SourceNode();
            sourceNode.setSource(expect(TokenType.IDENTIFIER).lexeme);
            if(isIn(TokenType.AS)) {
                advance();
                sourceNode.setAlias(expect(TokenType.IDENTIFIER).lexeme);
            }
            node.addSource(sourceNode);
        } else if(isIn(TokenType.LSPAREN)) {
            SourceNode sourceNode = new SourceNode();
            sourceNode.setSource(query());
            if(isIn(TokenType.AS)) {
                advance();
                sourceNode.setAlias(expect(TokenType.IDENTIFIER).lexeme);
            }
        } else {
            throw new ParseException("Expected IDENTIFIER, VIEW, or LSPAREN, found " + peek().type, pos);
        }
    }

    private void querySuffix(QueryNode query) throws ParseException {
        if(isIn(TokenType.WHERE)) {
            query.setWhere(where());
        }
        if(isIn(TokenType.GROUP_BY)) {
            groupBy();
        }
        if(isIn(TokenType.HAVING)) {
            having();
        }
        if(isIn(TokenType.ORDER_BY)) {
            orderBy();
        }
        if(isIn(TokenType.LIMIT)) {
            limit();
        }
    }

    private WhereNode where() throws ParseException {
        WhereNode whereNode = new WhereNode();
        if(isIn(TokenType.WHERE)) {
            advance();
            whereNode.setCondition(whereOr());
            return whereNode;
        } else {
            throw new ParseException("Expected WHERE keyword, found " + peek().type, pos);
        }
    }

    private AstNode whereOr() throws ParseException {
        AstNode left = whereAnd();
        while(isIn(TokenType.OR)) {
            advance();
            AstNode right = whereAnd();
            OrNode res = new OrNode();
            res.setLeft(left);
            res.setRight(right);
            left = res;
        }
        return left;
    }

    private AstNode whereAnd() throws ParseException {
        AstNode left = condition();
        while(isIn(TokenType.AND)) {
            advance();
            AstNode right = condition();
            AndNode res = new AndNode();
            res.setLeft(left);
            res.setRight(right);
            left = res;
        }
        return left;
    }

    private AstNode condition() throws ParseException {
        if(isIn(TokenType.IDENTIFIER) && (lookahead(1).type != TokenType.DOT && lookahead(1).type != TokenType.ASSIGNMENT) || isIn(TokenType.NUMBER, TokenType.LPAREN, TokenType.PLUS, TokenType.MINUS)) {
            ConditionNode conditionNode = new ConditionNode();
            conditionNode.setLeft(arithmetic());
            conditionNode.setOperator(expect(TokenType.EE, TokenType.NEQ, TokenType.LT, TokenType.GT, TokenType.LE, TokenType.GE, TokenType.LIKE, TokenType.IN).lexeme);
            conditionNode.setRight(arithmetic());
            return conditionNode;
        } else if(isIn(TokenType.IDENTIFIER) && lookahead(1).type == TokenType.DOT) {
            GCCorrelationNode gcCorrelationNode = new GCCorrelationNode();
            gcCorrelationNode.setIdentifier(expect(TokenType.IDENTIFIER).lexeme);
            advance();
            gcCorrelationNode.setCorrelation(expect(TokenType.IDENTIFIER).lexeme);
            ConditionNode conditionNode = new ConditionNode();
            conditionNode.setLeft(gcCorrelationNode);
            conditionNode.setOperator(expect(TokenType.EE, TokenType.NEQ, TokenType.LT, TokenType.GT, TokenType.LE, TokenType.GE, TokenType.LIKE, TokenType.IN).lexeme);
            conditionNode.setRight(arithmetic());
            return conditionNode;
        } else if(isIn(TokenType.IDENTIFIER) && lookahead(1).type == TokenType.ASSIGNMENT) {
            return assignment();
        } else {
            throw new ParseException("Expected IDENTIFIER, NUMBER, LPAREN, PLUS, or MINUS, found " + peek().type, pos);
        }
    }

    private void groupBy() throws ParseException {
        if(isIn(TokenType.GROUP_BY)) {
            advance();
            expect(TokenType.IDENTIFIER);
            while(isIn(TokenType.COMMA)) {
                advance();
                expect(TokenType.IDENTIFIER);
            }
            if(isIn(TokenType.HAVING, TokenType.ORDER_BY, TokenType.LIMIT, TokenType.EOQ, TokenType.EOF)) {
                return;
            } else {
                throw new ParseException("Expected HAVING, ORDER BY, LIMIT, or EOF after GROUP BY, found " + peek().type, pos);
            }
        }
    }

    private void having() throws ParseException {
        if(isIn(TokenType.HAVING)) {
            advance();
            condition();
            while(isIn(TokenType.AND, TokenType.OR)) {
                advance();
                condition();
            }
            if(isIn(TokenType.ORDER_BY, TokenType.LIMIT, TokenType.EOQ, TokenType.EOF)) {
                return;
            } else {
                throw new ParseException("Expected ORDER BY, LIMIT, or EOF after HAVING condition, found " + peek().type, pos);
            }
        } else {
            throw new ParseException("Expected HAVING keyword, found " + peek().type, pos);
        }
    }

    private void orderBy() throws ParseException {
        if(isIn(TokenType.ORDER_BY)) {
            advance();
            expect(TokenType.IDENTIFIER);
            if(isIn(TokenType.ASC, TokenType.DESC)) {
                advance(); // Optional ASC or DESC
            }
            while (isIn(TokenType.COMMA)) {
                advance();
                expect(TokenType.IDENTIFIER);
                if(isIn(TokenType.ASC, TokenType.DESC)) {
                    advance(); // Optional ASC or DESC
                }
            }
            if(isIn(TokenType.LIMIT, TokenType.EOQ, TokenType.EOF)) {
                return;
            } else {
                throw new ParseException("Expected LIMIT or EOF after ORDER BY, found " + peek().type, pos);
            }
        } else {
            throw new ParseException("Expected ORDER BY keyword, found " + peek().type, pos);
        }
    }

    private void limit() throws ParseException {
        if(isIn(TokenType.LIMIT)) {
            advance();
            expression();
        } else if(isIn(TokenType.EOQ, TokenType.EOF)) {
            return; // No LIMIT clause
        } else {
            throw new ParseException("Expected LIMIT keyword or EOF, found " + peek().type, pos);
        }
    }

    private AstNode arithmetic() throws ParseException {
        if(isIn(TokenType.FUNCTION, TokenType.PLUS, TokenType.MINUS, TokenType.LPAREN, TokenType.IDENTIFIER, TokenType.NUMBER)) {
            AstNode left = term();
            return arithmeticPrime(left);
        } else {
            throw new ParseException("Expected PLUS, MINUS, LPAREN, FUNCTION, IDENTIFIER, or NUMBER for arithmetic expression, found " + peek().type, pos);
        }
    }

    private AstNode arithmeticPrime(AstNode left) throws ParseException {
        if(isIn(TokenType.PLUS, TokenType.MINUS)) {
            String operator = expect(TokenType.PLUS, TokenType.MINUS).lexeme;
            AstNode right = term();
            return new BinaryOpNode(operator, left, right);
        } else if(isIn(TokenType.EE, TokenType.NEQ, TokenType.LT, TokenType.GT, TokenType.LE, TokenType.GE, TokenType.LIKE, TokenType.IN, TokenType.AND, TokenType.OR, TokenType.RPAREN, TokenType.COMMA, TokenType.AS, TokenType.FROM, TokenType.GROUP_BY, TokenType.ORDER_BY, TokenType.HAVING, TokenType.LIMIT, TokenType.EOQ, TokenType.EOF)){
            return left;
        } else {
            throw new ParseException("Expected PLUS, MINUS, EE, NEQ, LT, GT, LE, GE, LIKE, IN, AND, OR, RPAREN, COMMA, AS, or EOF after term, found " + peek().type, pos);
        }
    }

    private AstNode term() throws ParseException {
        if(isIn(TokenType.FUNCTION, TokenType.LPAREN, TokenType.MINUS, TokenType.PLUS, TokenType.IDENTIFIER, TokenType.NUMBER)) {
            AstNode left = factor();
            return termPrime(left);
        } else  {
            throw new ParseException("Expected LPAREN, IDENTIFIER, FUNCTION, NUMBER, or FUNCTION for term, found " + peek().type, pos);
        }
    }

    private AstNode termPrime(AstNode left) throws ParseException {
        if(isIn(TokenType.MULT, TokenType.DIV)) {
            String operator = expect(TokenType.MULT, TokenType.DIV).lexeme;
            AstNode right = factor();
            return new BinaryOpNode(operator, left, right);
        } else if(isIn(TokenType.PLUS, TokenType.MINUS, TokenType.EE, TokenType.NEQ, TokenType.LT, TokenType.GT, TokenType.LE, TokenType.GE, TokenType.LIKE, TokenType.IN, TokenType.AND, TokenType.OR, TokenType.RPAREN, TokenType.COMMA, TokenType.AS, TokenType.FROM, TokenType.GROUP_BY, TokenType.ORDER_BY, TokenType.HAVING, TokenType.LIMIT, TokenType.EOQ, TokenType.EOF)) {
            return left; // No more terms
        } else {
            throw new ParseException("Expected MULT, DIV, PLUS, MINUS, EE, NEQ, LT, GT, LE, GE, LIKE, IN, AND, OR, RPAREN, COMMA, AS or EOF after factor term, got " + peek().type, pos);
        }
    }

    private AstNode factor() throws ParseException {
        if(isIn(TokenType.FUNCTION, TokenType.PLUS, TokenType.MINUS, TokenType.LPAREN, TokenType.IDENTIFIER, TokenType.NUMBER)) {
            AstNode right = power();
            return factorPrime(right);
        } else {
            throw new ParseException("Expected PLUS, MINUS, LPAREN, FUNCTION, IDENTIFIER, or NUMBER for factor, found " + peek().type, pos);
        }
    }

    private AstNode factorPrime(AstNode right) throws ParseException {
        if(isIn(TokenType.EXP)) {
            String operator = expect(TokenType.EXP).lexeme;
            AstNode left = power();
            return new BinaryOpNode(operator, left, right);
        } else if(isIn(TokenType.MULT, TokenType.DIV, TokenType.PLUS, TokenType.MINUS, TokenType.EE, TokenType.NEQ, TokenType.LT, TokenType.GT, TokenType.LE, TokenType.GE, TokenType.LIKE, TokenType.IN, TokenType.AND, TokenType.OR, TokenType.RPAREN, TokenType.COMMA, TokenType.AS, TokenType.FROM, TokenType.GROUP_BY, TokenType.ORDER_BY, TokenType.HAVING, TokenType.LIMIT, TokenType.EOQ, TokenType.EOF)) {
            return right; // No more factors
        } else {
            throw new ParseException("Expected EXP, MULT, DIV, PLUS, MINUS, EE, NEQ, LT, GT, LE, GE, LIKE, IN, AND, OR, RPAREN, COMMA, AS or EOF after power factor, got " + peek().type, pos);
        }
    }

    private AstNode power() throws ParseException {
        if(isIn(TokenType.FUNCTION, TokenType.PLUS, TokenType.MINUS, TokenType.LPAREN, TokenType.IDENTIFIER, TokenType.NUMBER)) {
            return unary();
        } else {
            throw new ParseException("Expected PLUS, MINUS, LPAREN, IDENTIFIER, FUnCTION, or NUMBER for power, found " + peek().type, pos);
        }
    }

    private AstNode unary() throws ParseException {
        if(isIn(TokenType.PLUS, TokenType.MINUS)) {
            String operator = expect(TokenType.PLUS, TokenType.MINUS).lexeme;
            AstNode operand = primary();
            return new UnaryOpNode(operator, operand);
        } else if(isIn(TokenType.FUNCTION, TokenType.LPAREN, TokenType.IDENTIFIER, TokenType.NUMBER)) {
            return primary();
        } else {
            throw new ParseException("Expected PLUS, MINUS, LPAREN, IDENTIFIER, FUNCTION, or NUMBER for unary operation, found " + peek().type, pos);
        }
    }

    private AstNode primary() throws ParseException {
        if(isIn(TokenType.IDENTIFIER)) {
            return new IdentifierNode(expect(TokenType.IDENTIFIER).lexeme);
        } else if(isIn(TokenType.NUMBER)) {
            return new NumberNode(expect(TokenType.NUMBER).lexeme);
        } else if(isIn(TokenType.LPAREN)) {
            advance(); // Consume LPAREN
            AstNode arithmeticNode = arithmetic();
            expect(TokenType.RPAREN);
            return arithmeticNode;
        } else if(isIn(TokenType.FUNCTION)) {
            return functionCall();
        } else {
            throw new ParseException("Expected IDENTIFIER, NUMBER, LPAREN, or FUNCTION for primary expression, found " + peek().type, pos);
        }
    }

    private FunctionNode functionCall() throws ParseException {
        FunctionNode functionNode = new FunctionNode();
        if(isIn(TokenType.FUNCTION)) {
            functionNode.setName(expect(TokenType.FUNCTION).lexeme);
            expect(TokenType.LPAREN);
            if(isIn(TokenType.IDENTIFIER, TokenType.NUMBER, TokenType.STRING, TokenType.BOOLEAN, TokenType.PLUS, TokenType.MINUS, TokenType.LPAREN, TokenType.LSPAREN)) {
                functionNode.addArgument(expression());
                while(isIn(TokenType.COMMA)) {
                    advance();
                    functionNode.addArgument(expression());
                }
            }
            expect(TokenType.RPAREN);
            return functionNode;
        } else {
            throw new ParseException("Expected FUNCTION keyword, found " + peek().type, pos);
        }
    }

    // === Token Helpers ===

    private Token peek() {
        return pos < tokens.size() ? tokens.get(pos) : new Token(TokenType.EOF, "");
    }


    private Token advance() {
        return tokens.get(pos++);
    }

    private Token lookahead(int offset) {
        int lookaheadPos = pos + offset;
        if (lookaheadPos < tokens.size()) {
            return tokens.get(lookaheadPos);
        }
        return new Token(TokenType.EOF, "");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (peek().type == type) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token expect(TokenType... types) {
        for (TokenType type : types) {
            if (peek().type == type) {
                return advance();
            }
        }
        throw new RuntimeException("Expected one of " + Arrays.toString(types) + " but found " + peek().type);
    }

    private boolean isIn(TokenType... types) {
        for (TokenType type : types) {
            if (peek().type == type) {
                return true;
            }
        }
        return false;
    }
}