package crypticlib.script.lex;

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

        public boolean isOperator() {
            return this == EQ || this == NEQ || this == GT || this == GTE
                || this == LT || this == LTE || this == AND || this == OR
                || this == PLUS || this == MINUS || this == MULTIPLY || this == DIVIDE;
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
