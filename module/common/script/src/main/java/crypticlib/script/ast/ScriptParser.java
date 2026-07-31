package crypticlib.script.ast;

import crypticlib.script.InterpolationPart;
import crypticlib.script.ScriptException;
import crypticlib.script.lex.Token;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 语法分析器
 * 将 Token 流解析为 AST
 *
 * 语法（EBNF）:
 *   program       = statement*
 *   statement     = if_stmt | var_assignment | direct_assignment | expression NEWLINE
 *   if_stmt       = "if" expression NEWLINE block
 *                   ("elseif" expression NEWLINE block)*
 *                   ("else" NEWLINE block)?
 *                   "endif" NEWLINE
 *   var_assignment = "var" IDENTIFIER "=" expression NEWLINE
 *   direct_assignment = IDENTIFIER "=" expression NEWLINE
 *   block         = statement*
 *   expression    = or_expr
 *   or_expr       = and_expr ("||" and_expr)*
 *   and_expr      = comparison ("&&" comparison)*
 *   comparison    = additive (("==" | "!=" | ">" | ">=" | "<" | "<=") additive)?
 *   additive      = multiplicative (("+" | "-") multiplicative)*
 *   multiplicative = unary (("*" | "/" | "%") unary)*
 *   unary         = ("!" | "-") unary | call
 *   call          = IDENTIFIER ":" IDENTIFIER "(" args ")" method_chain?
 *                 | IDENTIFIER "(" args ")" method_chain?
 *                 | IDENTIFIER method_chain?
 *                 | atom method_chain?
 *   method_chain  = ("." IDENTIFIER "(" args ")")*
 *   args          = (expression ("," expression)*)?
 *   atom          = STRING | INTERPOLATED_STRING | NUMBER | INTEGER | BOOLEAN | IDENTIFIER
 *                 | "(" expression ")"
 */
public class ScriptParser {

    /** 递归下降的最大嵌套深度，超过则抛出 ScriptException，防止深嵌套脚本触发 StackOverflowError */
    private static final int MAX_DEPTH = 200;

    private final List<Token> tokens;
    private int pos;
    private int depth;

    public ScriptParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private void enterDepth() {
        if (++depth > MAX_DEPTH) {
            depth--;
            int line = isAtEnd() ? tokens.get(tokens.size() - 1).line() : tokens.get(pos).line();
            throw new ScriptException("Expression nesting too deep (max " + MAX_DEPTH + ") at line " + line);
        }
    }

    private void exitDepth() {
        depth--;
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
        if (check(Token.Type.VAR)) {
            return parseVarAssignment();
        }
        if (check(Token.Type.NEWLINE) || check(Token.Type.EOF)) {
            advance();
            return null;
        }
        // 支持 name = expression 直接赋值（不需要 var）
        if (check(Token.Type.IDENTIFIER) && checkNext(Token.Type.ASSIGN)) {
            return parseDirectAssignment();
        }
        ASTNode expr = parseExpression();
        expectNewlineOrEOF();
        return expr;
    }

    /**
     * 解析 var name = expression
     */
    private ASTNode parseVarAssignment() {
        int line = advance().line(); // 消费 var
        if (!check(Token.Type.IDENTIFIER)) {
            throw new ScriptException("Expected variable name after 'var' at line " + line);
        }
        String varName = advance().value();
        expect(Token.Type.ASSIGN, "Expected '=' after variable name");
        ASTNode value = parseExpression();
        expectNewlineOrEOF();
        return new ASTNode.VariableDeclarationNode(varName, value, line);
    }

    /**
     * 解析 name = expression（不需要 var 关键字，变量必须已声明）
     */
    private ASTNode parseDirectAssignment() {
        String varName = advance().value(); // 消费变量名
        int line = previous().line();
        expect(Token.Type.ASSIGN, "Expected '=' after variable name");
        ASTNode value = parseExpression();
        expectNewlineOrEOF();
        return new ASTNode.VariableAssignmentNode(varName, value, line);
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
        return parseIf(true);
    }

    private ASTNode.IfNode parseIf(boolean consumeKeyword) {
        enterDepth();
        try {
            return parseIfBody(consumeKeyword);
        } finally {
            exitDepth();
        }
    }

    private ASTNode.IfNode parseIfBody(boolean consumeKeyword) {
        int line;
        if (consumeKeyword) {
            line = advance().line(); // 消费 "if"
        } else {
            line = previous().line(); // "elseif" 已被 match() 消费，取其行号
        }
        ASTNode condition = parseExpression();
        expectNewlineOrEOF();

        List<ASTNode> thenBody = parseBlock();

        List<ASTNode> elseBody = new ArrayList<>();
        if (match(Token.Type.ELSEIF)) {
            // elseif → 递归解析为嵌套的 if，放在 else 分支里
            ASTNode.IfNode elif = parseIf(false);
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
        enterDepth();
        try {
            return parseOr();
        } finally {
            exitDepth();
        }
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
        ASTNode left = parseAdditive();
        if (matchAny(Token.Type.EQ, Token.Type.NEQ, Token.Type.GT, Token.Type.GTE, Token.Type.LT, Token.Type.LTE)) {
            String op = previous().value();
            int line = previous().line();
            ASTNode right = parseAdditive();
            return new ASTNode.BinaryOpNode(op, left, right, line);
        }
        return left;
    }

    private ASTNode parseAdditive() {
        ASTNode left = parseMultiplicative();
        while (matchAny(Token.Type.PLUS, Token.Type.MINUS)) {
            String op = previous().value();
            int line = previous().line();
            ASTNode right = parseMultiplicative();
            left = new ASTNode.BinaryOpNode(op, left, right, line);
        }
        return left;
    }

    private ASTNode parseMultiplicative() {
        ASTNode left = parseUnary();
        while (matchAny(Token.Type.MULTIPLY, Token.Type.DIVIDE, Token.Type.MODULO)) {
            String op = previous().value();
            int line = previous().line();
            ASTNode right = parseUnary();
            left = new ASTNode.BinaryOpNode(op, left, right, line);
        }
        return left;
    }

    private ASTNode parseUnary() {
        enterDepth();
        try {
            if (check(Token.Type.NOT)) {
                advance();
                int line = previous().line();
                ASTNode operand = parseUnary();
                return new ASTNode.UnaryOpNode("!", operand, line);
            }
            if (check(Token.Type.MINUS)) {
                advance();
                int line = previous().line();
                ASTNode operand = parseUnary();
                return new ASTNode.UnaryOpNode("-", operand, line);
            }
            return parseCall();
        } finally {
            exitDepth();
        }
    }

    private ASTNode parseCall() {
        if (check(Token.Type.IDENTIFIER)) {
            Token name = advance();
            String funcName = name.value();

            // 检查是否是 module:function 格式
            boolean moduleCall = false;
            if (match(Token.Type.COLON)) {
                if (!check(Token.Type.IDENTIFIER)) {
                    throw new ScriptException("Expected function name after ':' at line " + previous().line());
                }
                Token funcToken = advance();
                funcName = funcName + "." + funcToken.value();
                moduleCall = true;
            }

            // 模块调用必须带括号：module:function 后若无 '(' 就报错，
            // 否则会退化成名为 "module.function" 的变量引用（脚本无法声明这种名字），恒为 nil
            if (moduleCall && !check(Token.Type.LPAREN)) {
                throw new ScriptException("Module function '" + funcName.replace('.', ':')
                    + "' must be called with parentheses at line " + name.line());
            }

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
                ASTNode call = new ASTNode.FunctionCallNode(funcName, args, name.line());
                return parseMethodChain(call);
            }

            // 无参：后面没有括号，当作变量引用
            ASTNode varRef = new ASTNode.VariableReferenceNode(funcName, name.line());
            return parseMethodChain(varRef);
        }

        ASTNode atom = parseAtom();
        return parseMethodChain(atom);
    }

    /**
     * 解析 .method(args) 链式调用
     * 将 receiver 作为隐式第一个参数传给 method 函数
     */
    private ASTNode parseMethodChain(ASTNode receiver) {
        while (match(Token.Type.DOT)) {
            if (!check(Token.Type.IDENTIFIER)) {
                throw new ScriptException("Expected method name after '.' at line " + previous().line());
            }
            Token methodToken = advance();
            String methodName = methodToken.value();

            expect(Token.Type.LPAREN, "Expected '(' after method name");
            List<ASTNode> args = new ArrayList<>();
            if (!check(Token.Type.RPAREN)) {
                args.add(parseExpression());
                while (match(Token.Type.COMMA)) {
                    args.add(parseExpression());
                }
            }
            expect(Token.Type.RPAREN, "Expected ')'");

            // receiver 隐式作为第一个参数
            List<ASTNode> fullArgs = new ArrayList<>(args.size() + 1);
            fullArgs.add(receiver);
            fullArgs.addAll(args);

            receiver = new ASTNode.FunctionCallNode(methodName, fullArgs, methodToken.line());
        }
        return receiver;
    }

    private ASTNode parseAtom() {
        Token tok = advance();
        Token.Type type = tok.type();
        if (type == Token.Type.STRING) {
            return new ASTNode.LiteralNode(tok.value(), tok.line());
        } else if (type == Token.Type.INTERPOLATED_STRING) {
            // 将插值字符串的 parts 转换为 AST 节点列表
            List<InterpolationPart> rawParts = tok.interpolationParts();
            // 直接复用，无需转换
            List<InterpolationPart> astParts = new ArrayList<>(rawParts);
            return new ASTNode.StringInterpolationNode(astParts, tok.line());
        } else if (type == Token.Type.NUMBER) {
            return new ASTNode.LiteralNode(new BigDecimal(tok.value()), tok.line());
        } else if (type == Token.Type.INTEGER) {
            return new ASTNode.LiteralNode(Long.parseLong(tok.value()), tok.line());
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

    private boolean checkNext(Token.Type type) {
        return pos + 1 < tokens.size() && tokens.get(pos + 1).type() == type;
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
