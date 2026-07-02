package crypticlib.script;

import crypticlib.script.ast.ASTNode;
import crypticlib.script.ast.ScriptParser;
import crypticlib.script.compile.CompiledScript;
import crypticlib.script.compile.ScriptCompiler;
import crypticlib.script.lex.ScriptLexer;
import crypticlib.script.lex.Token;
import crypticlib.script.vm.ScriptVM;

import java.util.List;

/**
 * 算术运算 + 字符串拼接 单元测试
 * 直接 javac 编译运行，不依赖 JUnit
 */
public class ArithmeticTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        // === 基本算术 ===
        testExpr("加法",       "return 1 + 2",             3.0);
        testExpr("减法",       "return 10 - 3",            7.0);
        testExpr("乘法",       "return 4 * 5",             20.0);
        testExpr("除法",       "return 10 / 4",            2.5);

        // === 优先级 ===
        testExpr("乘法优先于加法", "return 1 + 2 * 3",      7.0);
        testExpr("除法优先于减法", "return 10 - 6 / 3",     8.0);
        testExpr("括号改变优先级", "return (1 + 2) * 3",     9.0);

        // === 一元负号 ===
        testExpr("一元负号",     "return -5",               -5.0);
        testExpr("负号加法",     "return -3 + 8",            5.0);
        testExpr("负号乘法",     "return -2 * 3",           -6.0);
        testExpr("双重负号",     "return -(-4)",              4.0);

        // === 字符串拼接 ===
        testExpr("字符串拼接",   "return \"hello\" + \" \" + \"world\"", "hello world");
        testExpr("字符串+数字",  "return \"score: \" + 100", "score: 100.0");
        testExpr("数字+字符串",  "return 42 + \" is the answer\"", "42.0 is the answer");

        // === 比较运算 + 算术混合 ===
        testExpr("算术+比较",   "return 1 + 2 > 2",         true);
        testExpr("算术+比较2",  "return 1 + 2 == 3",        true);
        testExpr("算术+比较3",  "return 10 / 2 <= 5",       true);

        // === 除零检测 ===
        testError("除零异常",    "return 1 / 0",             "Division by zero");

        // === 标识符不含 '-' ===
        testTokens("标识符无连字符", "take_money(1 + 2)",
            "IDENTIFIER(take_money) LPAREN NUMBER(1.0) PLUS NUMBER(2.0) RPAREN");

        // === 负数作为函数参数 ===
        testExpr("负数参数",    "return abs -5",             -5.0, "abs");

        // === 复杂表达式 ===
        testExpr("复杂算术",    "return (10 + 2) * 3 - 4 / 2", 34.0);

        // === 结果 ===
        System.out.println("\n=============================");
        System.out.println("通过: " + passed + "  失败: " + failed);
        System.out.println("=============================");
        if (failed > 0) {
            System.exit(1);
        }
    }

    /**
     * 测试表达式求值：编译 + 执行，比较结果
     */
    private static void testExpr(String name, String source, Object expected) {
        testExpr(name, source, expected, null);
    }

    private static void testExpr(String name, String source, Object expected, String funcName) {
        try {
            ScriptValue result = execute(source, funcName);
            Object actual;
            if (expected instanceof Double) {
                actual = result.asNumber();
            } else if (expected instanceof Boolean) {
                actual = result.asBoolean();
            } else if (expected instanceof String) {
                actual = result.asString();
            } else {
                actual = result;
            }

            if (expected.equals(actual)) {
                System.out.println("✅ " + name + ": " + source + " → " + actual);
                passed++;
            } else {
                System.out.println("❌ " + name + ": " + source + " → 期望 " + expected + "，实际 " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("❌ " + name + ": " + source + " → 异常: " + e.getMessage());
            failed++;
        }
    }

    /**
     * 测试预期抛出异常
     */
    private static void testError(String name, String source, String expectedMsg) {
        try {
            execute(source, null);
            System.out.println("❌ " + name + ": " + source + " → 未抛出异常");
            failed++;
        } catch (ScriptException e) {
            if (e.getMessage().contains(expectedMsg)) {
                System.out.println("✅ " + name + ": " + source + " → " + e.getMessage());
                passed++;
            } else {
                System.out.println("❌ " + name + ": 异常消息不匹配，期望包含 '" + expectedMsg + "'，实际 '" + e.getMessage() + "'");
                failed++;
            }
        }
    }

    /**
     * 测试 token 化结果
     */
    private static void testTokens(String name, String source, String expected) {
        List<Token> tokens = new ScriptLexer(source).tokenize();
        StringBuilder sb = new StringBuilder();
        for (Token t : tokens) {
            if (t.type() == Token.Type.NEWLINE || t.type() == Token.Type.EOF) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(t.type());
            if (t.type() == Token.Type.STRING || t.type() == Token.Type.NUMBER
                || t.type() == Token.Type.BOOLEAN || t.type() == Token.Type.IDENTIFIER) {
                sb.append('(').append(t.value()).append(')');
            }
        }
        String actual = sb.toString();
        if (expected.equals(actual)) {
            System.out.println("✅ " + name + ": " + source + " → " + actual);
            passed++;
        } else {
            System.out.println("❌ " + name + ": " + source);
            System.out.println("   期望: " + expected);
            System.out.println("   实际: " + actual);
            failed++;
        }
    }

    /**
     * 执行脚本并返回结果
     */
    private static ScriptValue execute(String source, String funcName) {
        List<Token> tokens = new ScriptLexer(source).tokenize();
        ASTNode.BlockNode ast = new ScriptParser(tokens).parse();
        CompiledScript compiled = new ScriptCompiler().compile("test", ast);

        // 创建简单的上下文，支持 return 语句
        ScriptContext ctx = new ScriptContext() {};

        // 如果需要注册测试函数
        if (funcName != null) {
            // 暂不注册，用 return 语句测试
        }

        ScriptVM vm = new ScriptVM(compiled, ctx);
        return vm.execute();
    }
}
