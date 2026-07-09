package crypticlib.script.lex;

import crypticlib.script.InterpolationPart;
import crypticlib.script.ScriptException;

import java.util.ArrayList;
import java.util.List;

/**
 * 词法分析器
 * 将源代码文本转换为 Token 流
 *
 * 支持：
 * - 引号字符串: "hello world"
 * - 字符串插值: "Hello ${name}!" (使用 ${variable} 语法)
 * - 变量引用: ${variable}
 * - 数字: 123, 3.14, -5
 * - 布尔: true, false
 * - 运算符: == != > >= < <= && || + - * / %
 * - 标识符: perm, command, my_func
 * - 控制流关键字: if, else, elseif, endif
 * - 注释: // 行注释
 */
public class ScriptLexer {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int pos;
    private int line = 1;

    public ScriptLexer(String source) {
        this.source = source;
    }

    /**
     * 执行词法分析
     * @return Token列表
     * @throws ScriptException 词法错误
     */
    public List<Token> tokenize() {
        tokens.clear();
        pos = 0;
        line = 1;

        while (pos < source.length()) {
            char c = source.charAt(pos);

            // 跳过空白（不换行）
            if (c == ' ' || c == '\t' || c == '\r') {
                pos++;
                continue;
            }

            // 换行
            if (c == '\n') {
                if (tokens.isEmpty() || lastToken().type() != Token.Type.NEWLINE) {
                    tokens.add(new Token(Token.Type.NEWLINE, "\\n", line));
                }
                line++;
                pos++;
                continue;
            }

            // 注释 //
            if (c == '/' && peek() == '/') {
                skipLineComment();
                continue;
            }

            // 引号字符串
            if (c == '"') {
                readString();
                continue;
            }

            // 数字
            if (isDigit(c)) {
                readNumber();
                continue;
            }

            // 变量引用 ${identifier}
            if (c == '$' && peek() == '{') {
                readVariable();
                continue;
            }

            // 运算符
            if (tryReadOperator()) continue;

            // 分隔符
            if (c == '(') { tokens.add(new Token(Token.Type.LPAREN, "(", line)); pos++; continue; }
            if (c == ')') { tokens.add(new Token(Token.Type.RPAREN, ")", line)); pos++; continue; }
            if (c == ',') { tokens.add(new Token(Token.Type.COMMA, ",", line)); pos++; continue; }
            if (c == '.') { tokens.add(new Token(Token.Type.DOT, ".", line)); pos++; continue; }

            // 标识符 / 关键字
            if (isAlpha(c) || c == '_') {
                readIdentifier();
                continue;
            }

            throw new ScriptException("Unexpected character '" + c + "' at line " + line);
        }

        // 确保以 EOF 结尾
        if (tokens.isEmpty() || lastToken().type() != Token.Type.NEWLINE) {
            tokens.add(new Token(Token.Type.NEWLINE, "\\n", line));
        }
        tokens.add(new Token(Token.Type.EOF, "", line));
        return tokens;
    }

    private Token lastToken() {
        return tokens.get(tokens.size() - 1);
    }

    private void skipLineComment() {
        pos += 2;
        while (pos < source.length() && source.charAt(pos) != '\n') pos++;
    }

    /**
     * 读取 ${identifier} 变量引用
     */
    private void readVariable() {
        pos += 2; // 跳过 ${
        int start = pos;
        while (pos < source.length() && (isAlphaNumeric(source.charAt(pos)) || source.charAt(pos) == '_')) {
            pos++;
        }
        if (pos >= source.length() || source.charAt(pos) != '}') {
            throw new ScriptException("Expected '}' after variable name at line " + line);
        }
        String varName = source.substring(start, pos);
        if (varName.isEmpty()) {
            throw new ScriptException("Empty variable name at line " + line);
        }
        pos++; // 跳过 }
        tokens.add(new Token(Token.Type.VARIABLE, varName, line));
    }

    private void readString() {
        pos++; // 跳过开头 "
        int startLine = line;
        StringBuilder sb = new StringBuilder();
        List<InterpolationPart> parts = new ArrayList<>();

        while (pos < source.length() && source.charAt(pos) != '"') {
            char c = source.charAt(pos);

            // 检查转义 \$
            if (c == '\\' && pos + 1 < source.length() && source.charAt(pos + 1) == '$') {
                sb.append('$');
                pos += 2;
                continue;
            }

            // 检查插值 ${
            if (c == '$' && pos + 1 < source.length() && source.charAt(pos + 1) == '{') {
                // 保存之前的文本
                if (sb.length() > 0) {
                    parts.add(new InterpolationPart.Text(sb.toString()));
                    sb = new StringBuilder();
                }

                // 读取变量
                pos += 2; // 跳过 ${
                int varStart = pos;
                while (pos < source.length() && (isAlphaNumeric(source.charAt(pos)) || source.charAt(pos) == '_')) {
                    pos++;
                }
                if (pos >= source.length() || source.charAt(pos) != '}') {
                    throw new ScriptException("Expected '}' after variable name at line " + line);
                }
                String varName = source.substring(varStart, pos);
                if (varName.isEmpty()) {
                    throw new ScriptException("Empty variable name in string interpolation at line " + line);
                }
                pos++; // 跳过 }
                parts.add(new InterpolationPart.Variable(varName));
                continue;
            }

            // 普通转义字符
            if (c == '\\' && pos + 1 < source.length()) {
                pos++;
                c = source.charAt(pos);
                switch (c) {
                    case 'n':  sb.append('\n'); break;
                    case 't':  sb.append('\t'); break;
                    case '"':  sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    default:   sb.append('\\').append(c); break;
                }
            } else {
                sb.append(c);
            }
            pos++;
        }

        if (pos >= source.length()) {
            throw new ScriptException("Unterminated string at line " + startLine);
        }
        pos++; // 跳过结尾 "

        // 如果没有插值，返回普通字符串
        if (parts.isEmpty()) {
            tokens.add(new Token(Token.Type.STRING, sb.toString(), startLine));
            return;
        }

        // 有插值，保存最后一个文本部分
        if (sb.length() > 0) {
            parts.add(new InterpolationPart.Text(sb.toString()));
        }

        // 生成插值字符串标记
        tokens.add(new Token(Token.Type.INTERPOLATED_STRING, parts, startLine));
    }

    private void readNumber() {
        int start = pos;
        if (source.charAt(pos) == '-') pos++;
        while (pos < source.length() && isDigit(source.charAt(pos))) pos++;
        boolean isFloat = false;
        if (pos < source.length() && source.charAt(pos) == '.') {
            isFloat = true;
            do {
                pos++;
            } while (pos < source.length() && isDigit(source.charAt(pos)));
        }
        Token.Type type = isFloat ? Token.Type.NUMBER : Token.Type.INTEGER;
        tokens.add(new Token(type, source.substring(start, pos), line));
    }

    private void readIdentifier() {
        int start = pos;
        while (pos < source.length() && (isAlphaNumeric(source.charAt(pos)) || source.charAt(pos) == '_')) {
            pos++;
        }
        String word = source.substring(start, pos);
        Token.Type type;
        switch (word) {
            case "if":
                type = Token.Type.IF;
                break;
            case "else":
                type = Token.Type.ELSE;
                break;
            case "elseif":
                type = Token.Type.ELSEIF;
                break;
            case "endif":
                type = Token.Type.ENDIF;
                break;
            case "return":
                type = Token.Type.RETURN;
                break;
            case "true":
            case "false":
                type = Token.Type.BOOLEAN;
                break;
            default:
                type = Token.Type.IDENTIFIER;
                break;
        }
        tokens.add(new Token(type, word, line));
    }

    private boolean tryReadOperator() {
        char c = source.charAt(pos);
        char next = peek();

        if (c == '=' && next == '=') { tokens.add(new Token(Token.Type.EQ, "==", line)); pos += 2; return true; }
        if (c == '!' && next == '=') { tokens.add(new Token(Token.Type.NEQ, "!=", line)); pos += 2; return true; }
        if (c == '!' && next != '=') { tokens.add(new Token(Token.Type.NOT, "!", line)); pos++; return true; }
        if (c == '>' && next == '=') { tokens.add(new Token(Token.Type.GTE, ">=", line)); pos += 2; return true; }
        if (c == '<' && next == '=') { tokens.add(new Token(Token.Type.LTE, "<=", line)); pos += 2; return true; }
        if (c == '&' && next == '&') { tokens.add(new Token(Token.Type.AND, "&&", line)); pos += 2; return true; }
        if (c == '|' && next == '|') { tokens.add(new Token(Token.Type.OR, "||", line)); pos += 2; return true; }
        if (c == '>') { tokens.add(new Token(Token.Type.GT, ">", line)); pos++; return true; }
        if (c == '<') { tokens.add(new Token(Token.Type.LT, "<", line)); pos++; return true; }
        // 算术运算符
        if (c == '+') { tokens.add(new Token(Token.Type.PLUS, "+", line)); pos++; return true; }
        if (c == '-') { tokens.add(new Token(Token.Type.MINUS, "-", line)); pos++; return true; }
        if (c == '*') { tokens.add(new Token(Token.Type.MULTIPLY, "*", line)); pos++; return true; }
        if (c == '/') { tokens.add(new Token(Token.Type.DIVIDE, "/", line)); pos++; return true; }
        if (c == '%') { tokens.add(new Token(Token.Type.MODULO, "%", line)); pos++; return true; }

        return false;
    }

    private char peek() {
        return pos + 1 < source.length() ? source.charAt(pos + 1) : '\0';
    }

    private static boolean isDigit(char c) { return c >= '0' && c <= '9'; }
    private static boolean isAlpha(char c) { return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'); }
    private static boolean isAlphaNumeric(char c) { return isAlpha(c) || isDigit(c); }
}
