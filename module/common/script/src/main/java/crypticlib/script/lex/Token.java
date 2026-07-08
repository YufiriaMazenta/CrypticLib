package crypticlib.script.lex;

import crypticlib.script.InterpolationPart;

import java.util.List;
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
        VARIABLE,       // ${identifier} 变量引用
        INTERPOLATED_STRING, // 包含插值的字符串 "Hello ${name}!"

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
        DOT,        // .

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
    private final List<InterpolationPart> interpolationParts; // INTERPOLATED_STRING 类型专用
    private final int line;

    public Token(Type type, String value, int line) {
        this.type = type;
        this.value = value;
        this.interpolationParts = null;
        this.line = line;
    }

    /**
     * 创建插值字符串 Token
     * @param type 必须是 INTERPOLATED_STRING
     * @param interpolationParts 插值部分列表
     * @param line 行号
     */
    public Token(Type type, List<InterpolationPart> interpolationParts, int line) {
        if (type != Type.INTERPOLATED_STRING) {
            throw new IllegalArgumentException("List constructor only supports INTERPOLATED_STRING type");
        }
        this.type = type;
        this.interpolationParts = interpolationParts;
        this.value = buildInterpolationPreview(interpolationParts);
        this.line = line;
    }

    public Type type() { return type; }
    public String value() { return value; }
    public int line() { return line; }

    /**
     * 获取插值字符串的部分列表
     * @return 插值部分列表，仅 INTERPOLATED_STRING 类型有值
     */
    public List<InterpolationPart> getInterpolationParts() {
        return interpolationParts;
    }

    /**
     * 构建插值字符串的预览文本（用于调试）
     */
    private static String buildInterpolationPreview(List<InterpolationPart> parts) {
        StringBuilder sb = new StringBuilder("\"");
        for (InterpolationPart part : parts) {
            sb.append(part);
        }
        sb.append("\"");
        return sb.toString();
    }

    @Override
    public String toString() {
        return type + "(" + value + ")@" + line;
    }
}
