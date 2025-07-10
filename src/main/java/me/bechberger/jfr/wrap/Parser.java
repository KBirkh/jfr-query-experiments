package me.bechberger.jfr.wrap;

import java.text.ParseException;
import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ProgramNode parse() throws ParseException {
        ProgramNode ast = program();
        if (peek().type != TokenType.EOF) {
            throw new ParseException("Unexpected token: " + peek().type + ", expected EOF", pos);
        }
        return ast;
    }

    public ProgramNode program() throws ParseException {
        ProgramNode prog = new ProgramNode();
        if(isIn(TokenType.IDENTIFIER, TokenType.VIEW, TokenType.AT, TokenType.SELECT, TokenType.COLUMN, TokenType.FORMAT)) {
            prog.addStatement(statement());
            prog.addTail(programTail());
            return prog;
        } else throw new ParseException("Unexpected token: "+peek().type, pos);
    }

    public ProgramNode programTail() throws ParseException {
        ProgramNode tail = new ProgramNode();
        if(isIn(TokenType.IDENTIFIER, TokenType.VIEW, TokenType.AT, TokenType.SELECT, TokenType.COLUMN, TokenType.FORMAT)) {
            tail.addStatement(statement());
            tail.addTail(programTail());
            return tail;
        } else if (peek().type == TokenType.EOF) {
            return null;
        } else {
            throw new ParseException("Unexpected token: " + peek().type, pos);
        }
    }

    public StatementNode statement() throws ParseException {
        StatementNode stmt = new StatementNode();
        if(isIn(TokenType.IDENTIFIER)) {
            stmt.addAssignment(assignment());
            return stmt;
        } else if(isIn(TokenType.VIEW)) {
            stmt.addViewDefinition(viewDefinition());
            return stmt;
        } else if(isIn(TokenType.AT, TokenType.SELECT, TokenType.COLUMN, TokenType.FORMAT)) {
            stmt.addQuery(query());
            return stmt;
        } else {
            throw new ParseException("Unexpected token: " + peek().type, pos);
        }
    }

    public AssignmentNode assignment() throws ParseException {
        AssignmentNode assign = new AssignmentNode();
        assign.setIdentifier(expect(TokenType.IDENTIFIER).lexeme);
        expect(TokenType.ASSIGNMENT);
        assign.setQuery(query());
        return assign;
    }

    public ViewDefinitionNode viewDefinition() throws ParseException {
        ViewDefinitionNode viewDef = new ViewDefinitionNode();
        expect(TokenType.VIEW);
        viewDef.setName(expect(TokenType.IDENTIFIER).lexeme);
        expect(TokenType.AS);
        viewDef.setQuery(query());
        return viewDef;
    }

    public QueryNode query() throws ParseException {
        QueryNode query = new QueryNode();
        if(isIn(TokenType.AT, TokenType.SELECT, TokenType.COLUMN, TokenType.FORMAT)) {
            queryPrefix(query);
            query.setSelect(select());
            query.setFrom(from());
            query.setWhere(where());
            query.setGroupBy(groupBy());
            query.setHaving(having());
            query.setOrderBy(orderBy());
            query.setLimit(limit());
            return query;
        } else {
            throw new ParseException("Unexpected token: " + peek().type, pos);
        }
    }

    public void queryPrefix(QueryNode query) throws ParseException {
        if(match(TokenType.AT)) {
            query.hasAt = true;
            query.setColumn(columnOpt());
        } else if(isIn(TokenType.COLUMN, TokenType.FORMAT, TokenType.SELECT)) {
            query.hasAt = false;
            query.setColumn(columnOpt());
        } else {
            throw new ParseException("Expected AT, COLUMN, or FORMAT but found " + peek().type, pos);
        }
    }

    public ColumnNode columnOpt() throws ParseException {
        ColumnNode column = new ColumnNode();
        if (isIn(TokenType.COLUMN)) {
            advance();
            column.setName(expect(TokenType.IDENTIFIER).lexeme);
            column.setTail(columnTail());
            return column;
        } else if(isIn(TokenType.FORMAT, TokenType.SELECT)) {
            return null;
        } else {
            throw new ParseException("Expected COLUMN, FORMAT, or SELECT but found " + peek().type, pos);
        }
    }

    public ColumnNode columnTail() throws ParseException {
        ColumnNode tail = new ColumnNode();
        if (isIn(TokenType.COMMA)) {
            advance();
            tail.setName(expect(TokenType.TEXT).lexeme);
            tail.setTail(columnTail());
            return tail;
        } else if (isIn(TokenType.FORMAT, TokenType.SELECT)) {
            return null;
        } else {
            throw new ParseException("Expected COMMA, FORMAT, or SELECT but found " + peek().type, pos);
        }
    }

    public SelectNode select() throws ParseException {
        SelectNode select = new SelectNode();
        if(isIn(TokenType.SELECT)) {
            advance();
            select.setSelectList(selectList());
        }
        return select;
    }

    public SelectListNode selectList() throws ParseException {
        SelectListNode list = new SelectListNode();
        if(isIn(TokenType.MULT)) {
            advance();
            list.isStar = true;
            return list;
        } else if(isIn(TokenType.FUNCTION, TokenType.IDENTIFIER, TokenType.TEXT, TokenType.LPAREN, TokenType.LSPAREN, TokenType.NUMBER)) {
            list.isStar = false;
            list.setExpression(expression());
            list.setSelectList(selectListTail());
            return list;
        } else {   
            throw new ParseException("Expected IDENTIFIER or MULT but found " + peek().type, pos);
        }
    }

    public SelectListNode selectListTail() throws ParseException {
        SelectListNode tail = new SelectListNode();
        if (isIn(TokenType.COMMA)) {
            advance();
            tail.setExpression(expression());
            return tail;
        } else if (isIn(TokenType.FROM)) {
            return null;
        } else {
            throw new ParseException("Expected COMMA or FROM but found " + peek().type, pos);
        }
    }

    public ExpressionNode expression() throws ParseException {
        ExpressionNode expr = new ExpressionNode();
        if(isIn(TokenType.NUMBER, TokenType.IDENTIFIER, TokenType.FUNCTION, TokenType.LPAREN)) {
            expr.setValue(arithmetic());
            if(match(TokenType.AS)) {
                expr.setAlias(expect(TokenType.IDENTIFIER).lexeme);
            }
            return expr;
        } else if(isIn(TokenType.LSPAREN)) {
            expr.setQuery(query());
            if(match(TokenType.AS)) {
                expr.setAlias(expect(TokenType.IDENTIFIER).lexeme);
            }
            return expr;
        } else throw new ParseException("Expected FUNCTION, IDENTIFIER, NUMBER, LPAREN, or LSPAREN but found " + peek().type, pos);
    }

    public FunctionNode function() throws ParseException {
        return new FunctionNode();
    }

    public AstNode arithmetic() throws ParseException {
        if(isIn(TokenType.MINUS, TokenType.PLUS, TokenType.LPAREN, TokenType.NUMBER, TokenType.IDENTIFIER)) {
            AstNode left = term();
            return arithmetic2(left);
        } else {
            throw new ParseException("Expected MINUS, PLUS, LPAREN, NUMBER, or IDENTIFIER but found " + peek().type, pos);
        }
    }

    public AstNode arithmetic2(AstNode left) throws ParseException {
        if(isIn(TokenType.PLUS, TokenType.MINUS)) {
            AstNode right = term();
            if(isIn(TokenType.PLUS, TokenType.MINUS)) {
                return new BinaryOpNode(expect(TokenType.PLUS, TokenType.MINUS).lexeme, term(), right);
            } else {
                return right;
            }
        } else if(isIn(TokenType.NEQ, TokenType.OR, TokenType.FROM, TokenType.COMMA, TokenType.AND, TokenType.EE, TokenType.EOF, TokenType.RPAREN, TokenType.WHERE, TokenType.GROUP_BY, TokenType.HAVING, TokenType.ORDER_BY, TokenType.LIMIT)) {
            return left;
        } else {
            throw new ParseException("Expected PLUS, MINUS, EOF, RPAREN, WHERE, GROUP BY, HAVING, ORDER BY, or LIMIT but found " + peek().type, pos);
        }
    }

    public AstNode term() throws ParseException {
        if(isIn(TokenType.LPAREN, TokenType.IDENTIFIER, TokenType.NUMBER, TokenType.PLUS, TokenType.MINUS)) {
            AstNode left = power();
            return term2(left);
        } else {
            throw new ParseException("Expected LPAREN, IDENTIFIER, NUMBER, PLUS, or MINUS but found " + peek().type, pos);
        }
    }

    public AstNode term2(AstNode left) throws ParseException {
        if(isIn(TokenType.MULT, TokenType.DIV)) {
            String operator = expect(TokenType.MULT, TokenType.DIV).lexeme;
            AstNode right = power();
            return term2(new BinaryOpNode(operator, left, right));
        } else if(isIn(TokenType.NEQ, TokenType.OR, TokenType.FROM, TokenType.COMMA, TokenType.AND, TokenType.EE, TokenType.PLUS, TokenType.MINUS, TokenType.EOF, TokenType.RPAREN, TokenType.WHERE, TokenType.GROUP_BY, TokenType.HAVING, TokenType.ORDER_BY, TokenType.LIMIT)) {
            return left;
        } else {
            throw new ParseException("Expected PLUS, MINUS, MULT, DIV, EOF, RPAREN, WHERE, GROUP BY, HAVING, ORDER BY, or LIMIT but found " + peek().type, pos);
        }
    }

    public AstNode power() throws ParseException {
        if(isIn(TokenType.LPAREN, TokenType.IDENTIFIER, TokenType.NUMBER, TokenType.PLUS, TokenType.MINUS)) {
            AstNode left = factor();
            return power2(left);
        } else {
            throw new ParseException("Expected LPAREN, IDENTIFIER, NUMBER, PLUS, or MINUS but found " + peek().type, pos);
        }
    }

    public AstNode power2(AstNode left) throws ParseException {
        if(isIn(TokenType.EXP)) {
            String operator = expect(TokenType.EXP).lexeme;
            AstNode right = factor();
            return power2(new BinaryOpNode(operator, left, right));
        } else if(isIn(TokenType.NEQ, TokenType.OR, TokenType.FROM, TokenType.COMMA, TokenType.AND, TokenType.EE, TokenType.MULT, TokenType.DIV, TokenType.PLUS, TokenType.MINUS, TokenType.EOF, TokenType.RPAREN, TokenType.WHERE, TokenType.GROUP_BY, TokenType.HAVING, TokenType.ORDER_BY, TokenType.LIMIT)) {
            return left;
        } else {
            throw new ParseException("Expected EXP, MULT, DIV, PLUS, MINUS, EOF, RPAREN, WHERE, GROUP BY, HAVING, ORDER BY, or LIMIT but found " + peek().type, pos);
        }
    }

    public AstNode factor() throws ParseException {
        if(isIn(TokenType.MINUS, TokenType.PLUS)) {
            String operator = expect(TokenType.MINUS, TokenType.PLUS).lexeme;
            AstNode operand = factor();
            return new UnaryOpNode(operator, operand);
        } else if(isIn(TokenType.LPAREN)) {
            advance();
            AstNode expr = arithmetic();
            expect(TokenType.RPAREN);
            return expr;
        } else if(isIn(TokenType.IDENTIFIER)) {
            String identifier = expect(TokenType.IDENTIFIER).lexeme;
            if(match(TokenType.LPAREN)) {
                FunctionNode function = new FunctionNode();
                function.setName(identifier);
                function.setArgument(functionArgs());
                return function;
            } else {
                return new IdentifierNode(identifier);
            }
        } else if(isIn(TokenType.NUMBER)) {
            return new NumberNode(expect(TokenType.NUMBER).lexeme);
        } else {
            throw new ParseException("Expected MINUS, LPAREN, IDENTIFIER, or NUMBER but found " + peek().type, pos);
        }
    }

    public AstNode functionArgs() {
        return new ProgramNode();   
    }

    // TODO: Expression Node shenanigans

    public FromNode from() throws ParseException {
        FromNode from = new FromNode();
        if(isIn(TokenType.FROM)) {
            advance();
            from.addSource(source());
            while(match(TokenType.COMMA)) {
                from.addSource(source());
            }
            if(isIn(TokenType.WHERE, TokenType.GROUP_BY, TokenType.HAVING, TokenType.ORDER_BY, TokenType.LIMIT, TokenType.EOF, TokenType.RSPAREN)) {
                return from;
            } else {
                throw new ParseException("Expected WHERE, GROUP BY, HAVING, ORDER BY, or LIMIT but found " + peek().type, pos);
            }
        } else {
            throw new ParseException("Expected FROM but found " + peek().type, pos);
        }
    }

    public SourceNode source() throws ParseException {
        SourceNode source = new SourceNode();
        if(isIn(TokenType.IDENTIFIER)) {
            source.setName(expect(TokenType.IDENTIFIER).lexeme);
            if(isIn(TokenType.AS)) {
                advance();
                source.setAlias(expect(TokenType.IDENTIFIER).lexeme);
            }
            return source;
        } else if(isIn(TokenType.LSPAREN)) {
            advance();
            source.setSubquery(query());
            expect(TokenType.RSPAREN);
            if(isIn(TokenType.AS)) {
                advance();
                source.setAlias(expect(TokenType.IDENTIFIER).lexeme);
            }
            return source;
        } else {
            throw new ParseException("Expected IDENTIFIER or LPAREN but found " + peek().type, pos);
        }
    }

    public WhereNode where() throws ParseException {
        WhereNode where = new WhereNode();
        if(isIn(TokenType.WHERE)) {
            advance();
            where.setCondition(condition());
            where.setTail(conditionTail());
            return where;
        } else if(isIn(TokenType.GROUP_BY, TokenType.HAVING, TokenType.ORDER_BY, TokenType.LIMIT, TokenType.EOF, TokenType.RSPAREN)) {
            return null;
        } else {
            throw new ParseException("Expected WHERE, GROUP_BY, HAVING, ORDER_BY, LIMIT, RSPAREN, or EOF but found " + peek().type, pos);
        }
    }

    public ConditionNode conditionTail() throws ParseException {
        ConditionNode tail = new ConditionNode();
        if (isIn(TokenType.AND, TokenType.OR)) {
            tail.setOperator(expect(TokenType.AND, TokenType.OR).lexeme);
            tail.setCondition(condition());
            tail.setTail(conditionTail());
            return tail;
        } else if (isIn(TokenType.GROUP_BY, TokenType.HAVING, TokenType.ORDER_BY, TokenType.LIMIT, TokenType.EOF)) {
            return null;
        } else {
            throw new ParseException("Expected AND, OR, GROUP_BY, HAVING, ORDER_BY, or LIMIT but found " + peek().type, pos);
        }
    }

    public ConditionNode condition() throws ParseException {
        ConditionNode cond = new ConditionNode();
        if(isIn(TokenType.FUNCTION, TokenType.IDENTIFIER, TokenType.LPAREN, TokenType.LSPAREN, TokenType.NUMBER)) {
            cond.setLeft(expression());
            cond.setOperator(peek().lexeme);
            advance();
            cond.setRight(expression());
            return cond;
        } else throw new ParseException("Expected FUNCTION, IDENTIFIER, LPAREN, or LSPAREN but found " + peek().type, pos);
    }

    public GroupByNode groupBy() throws ParseException {
        GroupByNode groupBy = new GroupByNode();
        if(isIn(TokenType.GROUP_BY)) {
            advance();
            groupBy.addGroup(expect(TokenType.IDENTIFIER).lexeme);
            while(match(TokenType.COMMA)) {
                groupBy.addGroup(expect(TokenType.IDENTIFIER).lexeme);
            }
            return groupBy;
        } else if(isIn(TokenType.HAVING, TokenType.ORDER_BY, TokenType.LIMIT, TokenType.EOF, TokenType.RSPAREN)) {
            return null;
        } else {
            throw new ParseException("Expected GROUP_BY, HAVING, ORDER_BY, LIMIT, RSPAREN, or EOF but found " + peek().type, pos);
        }
    }

    public HavingNode having() throws ParseException {
        HavingNode having = new HavingNode();
        if(isIn(TokenType.HAVING)) {
            advance();
            having.setCondition(condition());
            having.setTail(conditionTail());
            return having;
        } else if(isIn(TokenType.ORDER_BY, TokenType.LIMIT, TokenType.EOF, TokenType.RSPAREN)) {
            return null;
        } else {
            throw new ParseException("Expected HAVING, ORDER_BY, LIMIT, RSPAREN, or EOF but found " + peek().type, pos);
        }
    }

    public OrderByNode orderBy() throws ParseException {
        OrderByNode orderBy = new OrderByNode();
        if(isIn(TokenType.ORDER_BY)) {
            advance();
            orderBy.addOrder(expect(TokenType.IDENTIFIER).lexeme);
            while(match(TokenType.COMMA)) {
                orderBy.addOrder(expect(TokenType.IDENTIFIER).lexeme);
            }
            return orderBy;
        } else if(isIn(TokenType.LIMIT, TokenType.EOF, TokenType.RSPAREN)) {
            return null;
        } else {
            throw new ParseException("Expected ORDER_BY, LIMIT, RSPAREN, or EOF but found " + peek().type, pos);
        }
    }

    public LimitNode limit() throws ParseException {
        LimitNode limit = new LimitNode();
        if(isIn(TokenType.LIMIT)) {
            advance();
            if(isIn(TokenType.NUMBER)) {
                limit.setCount(expect(TokenType.NUMBER).lexeme);
            } else if(isIn(TokenType.IDENTIFIER, TokenType.EOF)) {
                limit.setCount(expect(TokenType.IDENTIFIER).lexeme);
            } else {
                throw new ParseException("Expected NUMBER or IDENTIFIER after LIMIT but found " + peek().type, pos);
            }
            return limit;
        } else {
            return null;
        }
    }

    // === Token Helpers ===

    private Token peek() {
        return pos < tokens.size() ? tokens.get(pos) : new Token(TokenType.EOF, "");
    }


    private Token advance() {
        return tokens.get(pos++);
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