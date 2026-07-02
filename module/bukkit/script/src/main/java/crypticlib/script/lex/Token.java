package crypticlib.script.lex;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 词法分析产出的Token
 */
public class Token {

    public enum Type {
        // 字面量
        STRING,     // "hello world"
        NUMBER,     // 123, 3.14
        BOOLEAN,    // true, false

        // 标识符（函数名、变量名）
        IDENTIFIER,

        // 运算符
        EQ,         // ==
        NEQ,        // !=
        GT,         // >
        GTE,        // >=
        LT,         // <
        LTE,        // <=
        AND,        // &&
        OR,         // ||
        NOT,        // !

        // 算术运算符
        PLUS,       // +
        MINUS,      // -
        MULTIPLY,   // *
        DIVIDE,     // /
        MODULO,     // %

        // 分隔符
        LPAREN,     // (
        RPAREN,     // )
        COMMA,      // ,

        // 控制流
        IF,
        ELSE,
        ELSEIF,
        ENDIF,
        RETURN,

        // 特殊
        NEWLINE,
        EOF;

        private static final Set<Type> OPERATOR_TYPES = Stream.of(
            EQ, NEQ, GT, GTE, LT, LTE, AND, OR, PLUS, MINUS, MULTIPLY, DIVIDE, MODULO
        ).collect(Collectors.toSet());

        public boolean isOperator() {
            return OPERATOR_TYPES.contains(this);
        }

    }

    private final Type type;
    private final String value;
    private final int line;

    public Token(Type type, String value, int line) {
        this.type = type;
        this.value = value;
        this.line = line;
    }

    public Type type() { return type; }
    public String value() { return value; }
    public int line() { return line; }

    @Override
    public String toString() {
        return type + "(" + value + ")@" + line;
    }
}
