package crypticlib.script.ast;

import crypticlib.script.ScriptException;
import crypticlib.script.lex.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * 语法分析器
 * 将 Token 流解析为 AST
 *
 * 语法（EBNF）:
 *   program       = statement*
 *   statement     = if_stmt | expression NEWLINE
 *   if_stmt       = "if" expression NEWLINE block
 *                   ("elseif" expression NEWLINE block)*
 *                   ("else" NEWLINE block)?
 *                   "endif" NEWLINE
 *   block         = statement*
 *   expression    = or_expr
 *   or_expr       = and_expr ("||" and_expr)*
 *   and_expr      = comparison ("&&" comparison)*
 *   comparison    = unary (("==" | "!=" | ">" | ">=" | "<" | "<=") unary)?
 *   unary         = "!" unary | call
 *   call          = IDENTIFIER "(" args ")" | IDENTIFIER bare_args | atom
 *   bare_args     = atom+
 *   args          = (expression ("," expression)*)?
 *   atom          = STRING | NUMBER | BOOLEAN | "(" expression ")"
 */
public class ScriptParser {

    private final List<Token> tokens;
    private int pos;

    public ScriptParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * 解析为 AST
     * @return 程序的根节点（BlockNode）
     */
    public ASTNode.BlockNode parse() {
        List<ASTNode> statements = new ArrayList<>();
        skipNewlines();
        while (!isAtEnd()) {
            ASTNode stmt = parseStatement();
            if (stmt != null) {
                statements.add(stmt);
            }
            skipNewlines();
        }
        int line = tokens.isEmpty() ? 1 : tokens.get(tokens.size() - 1).line();
        return new ASTNode.BlockNode(statements, line);
    }

    // ======================== 语句 ========================

    private ASTNode parseStatement() {
        if (check(Token.Type.IF)) {
            return parseIf();
        }
        if (check(Token.Type.RETURN)) {
            return parseReturn();
        }
        if (check(Token.Type.NEWLINE) || check(Token.Type.EOF)) {
            advance();
            return null;
        }
        ASTNode expr = parseExpression();
        expectNewlineOrEOF();
        return expr;
    }

    private ASTNode parseReturn() {
        int line = advance().line();
        if (check(Token.Type.NEWLINE) || check(Token.Type.EOF)) {
            expectNewlineOrEOF();
            return new ASTNode.ReturnNode(null, line);
        }
        ASTNode value = parseExpression();
        expectNewlineOrEOF();
        return new ASTNode.ReturnNode(value, line);
    }

    private ASTNode.IfNode parseIf() {
        int line = advance().line();
        ASTNode condition = parseExpression();
        expectNewlineOrEOF();

        List<ASTNode> thenBody = parseBlock();

        List<ASTNode> elseBody = new ArrayList<>();
        if (match(Token.Type.ELSEIF)) {
            ASTNode.IfNode elif = parseIf();
            elseBody.add(elif);
        } else if (match(Token.Type.ELSE)) {
            expectNewlineOrEOF();
            elseBody = parseBlock();
            expect(Token.Type.ENDIF, "Expected 'endif'");
            expectNewlineOrEOF();
        } else {
            expect(Token.Type.ENDIF, "Expected 'endif'");
            expectNewlineOrEOF();
        }

        return new ASTNode.IfNode(condition, thenBody, elseBody, line);
    }

    private List<ASTNode> parseBlock() {
        List<ASTNode> statements = new ArrayList<>();
        skipNewlines();
        while (!isAtEnd() && !check(Token.Type.ELSE) && !check(Token.Type.ELSEIF) && !check(Token.Type.ENDIF)) {
            ASTNode stmt = parseStatement();
            if (stmt != null) {
                statements.add(stmt);
            }
            skipNewlines();
        }
        return statements;
    }

    // ======================== 表达式（优先级递增） ========================

    private ASTNode parseExpression() {
        return parseOr();
    }

    private ASTNode parseOr() {
        ASTNode left = parseAnd();
        while (match(Token.Type.OR)) {
            int line = previous().line();
            ASTNode right = parseAnd();
            left = new ASTNode.BinaryOpNode("||", left, right, line);
        }
        return left;
    }

    private ASTNode parseAnd() {
        ASTNode left = parseComparison();
        while (match(Token.Type.AND)) {
            int line = previous().line();
            ASTNode right = parseComparison();
            left = new ASTNode.BinaryOpNode("&&", left, right, line);
        }
        return left;
    }

    private ASTNode parseComparison() {
        ASTNode left = parseUnary();
        if (matchAny(Token.Type.EQ, Token.Type.NEQ, Token.Type.GT, Token.Type.GTE, Token.Type.LT, Token.Type.LTE)) {
            String op = previous().value();
            int line = previous().line();
            ASTNode right = parseUnary();
            return new ASTNode.BinaryOpNode(op, left, right, line);
        }
        return left;
    }

    private ASTNode parseUnary() {
        if (check(Token.Type.IDENTIFIER) && "!".equals(peek().value())) {
            advance();
            int line = previous().line();
            ASTNode operand = parseUnary();
            return new ASTNode.UnaryOpNode("!", operand, line);
        }
        return parseCall();
    }

    private ASTNode parseCall() {
        if (check(Token.Type.IDENTIFIER)) {
            Token name = advance();

            // 情况1: 有括号的函数调用 name(...)
            if (match(Token.Type.LPAREN)) {
                List<ASTNode> args = new ArrayList<>();
                if (!check(Token.Type.RPAREN)) {
                    args.add(parseExpression());
                    while (match(Token.Type.COMMA)) {
                        args.add(parseExpression());
                    }
                }
                expect(Token.Type.RPAREN, "Expected ')'");
                return new ASTNode.FunctionCallNode(name.value(), args, name.line());
            }

            // 情况2/3: 判断后面是否跟着可作为参数的 token（STRING/NUMBER/BOOLEAN/IDENTIFIER）
            // 如果是，收集为裸参数；否则是无参调用
            // IDENTIFIER 作为参数时会递归解析为函数调用（如 papi "%player_name%"）
            List<ASTNode> args = new ArrayList<>();
            while (isBareArgToken()) {
                if (check(Token.Type.IDENTIFIER)) {
                    args.add(parseCall());
                } else {
                    args.add(parseAtom());
                }
            }
            return new ASTNode.FunctionCallNode(name.value(), args, name.line());
        }

        return parseAtom();
    }

    private boolean isBareArgToken() {
        if (isAtEnd()) return false;
        Token.Type type = tokens.get(pos).type();
        return type == Token.Type.STRING || type == Token.Type.NUMBER || type == Token.Type.BOOLEAN;
    }

    private ASTNode parseAtom() {
        Token tok = advance();
        Token.Type type = tok.type();
        if (type == Token.Type.STRING) {
            return new ASTNode.LiteralNode(tok.value(), tok.line());
        } else if (type == Token.Type.NUMBER) {
            return new ASTNode.LiteralNode(Double.parseDouble(tok.value()), tok.line());
        } else if (type == Token.Type.BOOLEAN) {
            return new ASTNode.LiteralNode(Boolean.parseBoolean(tok.value()), tok.line());
        } else if (type == Token.Type.IDENTIFIER) {
            return new ASTNode.IdentifierNode(tok.value(), tok.line());
        } else if (type == Token.Type.LPAREN) {
            ASTNode expr = parseExpression();
            expect(Token.Type.RPAREN, "Expected ')'");
            return expr;
        } else {
            throw new ScriptException("Unexpected token " + tok + " at line " + tok.line());
        }
    }

    // ======================== 工具方法 ========================

    private Token advance() {
        return tokens.get(pos++);
    }

    private Token previous() {
        return tokens.get(pos - 1);
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private boolean check(Token.Type type) {
        return !isAtEnd() && tokens.get(pos).type() == type;
    }

    private boolean match(Token.Type type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    private boolean matchAny(Token.Type... types) {
        for (Token.Type type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private void expect(Token.Type type, String message) {
        if (!check(type)) {
            int line = isAtEnd() ? tokens.get(tokens.size() - 1).line() : tokens.get(pos).line();
            throw new ScriptException(message + " at line " + line);
        }
        advance();
    }

    private void expectNewlineOrEOF() {
        if (!isAtEnd() && !check(Token.Type.NEWLINE) && !check(Token.Type.EOF)) {
            throw new ScriptException("Expected end of line at line " + tokens.get(pos).line());
        }
        if (check(Token.Type.NEWLINE)) advance();
    }

    private void skipNewlines() {
        while (check(Token.Type.NEWLINE)) advance();
    }

    private boolean isAtEnd() {
        return pos >= tokens.size() || tokens.get(pos).type() == Token.Type.EOF;
    }
}
