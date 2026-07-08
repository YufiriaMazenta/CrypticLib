package crypticlib.script;

import crypticlib.script.ast.ASTNode;
import crypticlib.script.ast.ScriptParser;
import crypticlib.script.compile.CompiledScript;
import crypticlib.script.compile.ScriptCompiler;
import crypticlib.script.func.MathScriptModule;
import crypticlib.script.func.ScriptFunctionRegistry;
import crypticlib.script.lex.ScriptLexer;
import crypticlib.script.lex.Token;
import crypticlib.script.vm.ScriptVM;

import java.util.List;
import java.util.UUID;

/**
 * 变量插值功能测试
 * 测试 ${variable} 语法和字符串插值功能
 */
public class ScriptTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        // 注册数学函数模块
        MathScriptModule.INSTANCE.register(ScriptFunctionRegistry.INSTANCE);

        System.out.println("=== Script 变量插值功能测试 ===\n");

        // === 基本变量引用 ===
        System.out.println("--- 基本变量引用 ---");
        testVarRef("基本变量引用", "${x}", 10.0, "x", 10.0);
        testVarRef("字符串变量", "${name}", "Steve", "name", "Steve");
        testVarRef("布尔变量", "${flag}", true, "flag", true);
        testVarRef("变量在表达式中", "${x} > 5", true, "x", 10.0);
        testVarRef("变量比较相等", "${x} == 10", true, "x", 10.0);
        testVarRef("变量算术运算", "${x} + 5", 15.0, "x", 10.0);
        testVarRef("变量乘法", "${x} * 2", 20.0, "x", 10.0);

        // === 字符串插值 ===
        System.out.println("\n--- 字符串插值 ---");
        testInterpolation("简单插值", "\"Hello ${name}!\"", "Hello Steve!", "name", "Steve");
        testInterpolation("数字插值", "\"damage: ${d}\"", "damage: 10.0", "d", 10.0);
        testInterpolation("多变量插值", "\"${a} and ${b}\"", "Alice and Bob", "a", "Alice", "b", "Bob");
        testInterpolation("插值在开头", "\"${name} said\"", "Steve said", "name", "Steve");
        testInterpolation("插值在结尾", "\"Hello ${name}\"", "Hello Steve", "name", "Steve");
        testInterpolation("连续插值", "\"${a}${b}${c}\"", "123", "a", "1", "b", "2", "c", "3");
        testInterpolation("空字符串插值", "\"${empty}\"", "", "empty", "");

        // === 转义测试 ===
        System.out.println("\n--- 转义测试 ---");
        testVarRef("转义美元符号", "\"show \\${price}\"", "show ${price}");

        // === 未定义变量 ===
        System.out.println("\n--- 未定义变量 ---");
        testVarRef("未定义变量返回nil", "${undefined}", null);
        testInterpolation("未定义变量插值为空", "\"Hello ${undefined}!\"", "Hello !");

        // === 复杂场景 ===
        System.out.println("\n--- 复杂场景 ---");
        testVarRefWithMultipleVars("复杂条件判断",
            "${hp} > 0 && ${hp} < 100",
            true, "hp", 50.0);
        testVarRefWithMultipleVars("复杂字符串插值",
            "\"${attacker} hit ${victim} for ${damage} damage\"",
            "Steve hit Alex for 10.0 damage",
            "attacker", "Steve", "victim", "Alex", "damage", 10.0);

        // === Token 类型测试 ===
        System.out.println("\n--- Token 类型测试 ---");
        testTokenTypes("变量引用Token", "${x}", "VARIABLE(x)");
        testTokenTypes("插值字符串Token", "\"Hello ${name}!\"", "INTERPOLATED_STRING");

        // === 数学函数测试 ===
        System.out.println("\n--- 数学函数测试 ---");
        testMath("abs正数", "abs(5)", 5.0);
        testMath("abs负数", "abs(-5)", 5.0);
        testMath("abs裸参数正数", "abs 5", 5.0);
        testMath("abs裸参数负数", "abs -5", 5.0);
        testMath("min函数", "min(3, 7)", 3.0);
        testMath("max函数", "max(3, 7)", 7.0);
        testMath("round四舍五入", "round(3.5)", 4.0);
        testMath("round向下", "round(3.4)", 3.0);
        testMath("floor函数", "floor(3.7)", 3.0);
        testMath("ceil函数", "ceil(3.2)", 4.0);
        testMath("sqrt函数", "sqrt(16)", 4.0);
        testMath("pow函数", "pow(2, 3)", 8.0);

        // === 命名空间测试 ===
        System.out.println("\n--- 命名空间测试 ---");
        testMath("math.abs带命名空间", "math.abs(-5)", 5.0);
        testMath("math.min带命名空间", "math.min(3, 7)", 3.0);
        testMath("math.max带命名空间", "math.max(3, 7)", 7.0);
        testMath("math.sqrt带命名空间", "math.sqrt(16)", 4.0);
        testMath("math.pow带命名空间", "math.pow(2, 3)", 8.0);

        // === 结果 ===
        System.out.println("\n=============================");
        System.out.println("通过: " + passed + "  失败: " + failed);
        System.out.println("=============================");
    }

    /**
     * 测试变量引用（单个变量）
     */
    private static void testVarRef(String name, String source, Object expected, String varName, Object varValue) {
        try {
            ScriptContext ctx = createContext();
            if (varName != null) {
                ctx.setVariable(varName, toScriptValue(varValue));
            }
            ScriptValue result = execute(source, ctx);
            Object actual = toExpected(result, expected);

            if (expected == null) {
                // 期望 nil
                if (result.isNull()) {
                    System.out.println("✓ " + name + ": " + source + " → nil");
                    passed++;
                } else {
                    System.out.println("✗ " + name + ": " + source + " → 期望 nil，实际 " + result);
                    failed++;
                }
            } else if (expected.equals(actual)) {
                System.out.println("✓ " + name + ": " + source + " → " + actual);
                passed++;
            } else {
                System.out.println("✗ " + name + ": " + source + " → 期望 " + expected + "，实际 " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("✗ " + name + ": " + source + " → 异常: " + e.getMessage());
            failed++;
        }
    }

    /**
     * 测试变量引用（无预设变量，用于转义测试等）
     */
    private static void testVarRef(String name, String source, Object expected) {
        testVarRef(name, source, expected, null, null);
    }

    /**
     * 测试字符串插值
     */
    private static void testInterpolation(String name, String source, Object expected, Object... vars) {
        try {
            ScriptContext ctx = createContext();
            for (int i = 0; i < vars.length; i += 2) {
                ctx.setVariable((String) vars[i], toScriptValue(vars[i + 1]));
            }
            ScriptValue result = execute(source, ctx);
            String actual = result.asString();

            if (expected.equals(actual)) {
                System.out.println("✓ " + name + ": " + source + " → \"" + actual + "\"");
                passed++;
            } else {
                System.out.println("✗ " + name + ": " + source + " → 期望 \"" + expected + "\"，实际 \"" + actual + "\"");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("✗ " + name + ": " + source + " → 异常: " + e.getMessage());
            failed++;
        }
    }

    /**
     * 测试多变量引用的表达式
     */
    private static void testVarRefWithMultipleVars(String name, String source, Object expected, Object... vars) {
        try {
            ScriptContext ctx = createContext();
            for (int i = 0; i < vars.length; i += 2) {
                ctx.setVariable((String) vars[i], toScriptValue(vars[i + 1]));
            }
            ScriptValue result = execute(source, ctx);
            Object actual;
            if (expected instanceof Boolean) {
                actual = result.asBoolean();
            } else if (expected instanceof Double) {
                actual = result.asNumber();
            } else {
                actual = result.asString();
            }

            if (expected.equals(actual)) {
                System.out.println("✓ " + name + ": " + source + " → " + actual);
                passed++;
            } else {
                System.out.println("✗ " + name + ": " + source + " → 期望 " + expected + "，实际 " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("✗ " + name + ": " + source + " → 异常: " + e.getMessage());
            failed++;
        }
    }

    /**
     * 测试数学函数
     */
    private static void testMath(String name, String source, double expected) {
        try {
            ScriptContext ctx = createContext();
            ScriptValue result = execute(source, ctx);
            double actual = result.asNumber();

            if (Math.abs(expected - actual) < 0.0001) {
                System.out.println("✓ " + name + ": " + source + " → " + actual);
                passed++;
            } else {
                System.out.println("✗ " + name + ": " + source + " → 期望 " + expected + "，实际 " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("✗ " + name + ": " + source + " → 异常: " + e.getMessage());
            failed++;
        }
    }

    /**
     * 测试 Token 类型
     */
    private static void testTokenTypes(String name, String source, String expected) {
        try {
            List<Token> tokens = new ScriptLexer(source).tokenize();
            StringBuilder sb = new StringBuilder();
            for (Token t : tokens) {
                if (t.type() == Token.Type.NEWLINE || t.type() == Token.Type.EOF) continue;
                if (sb.length() > 0) sb.append(' ');
                sb.append(t.type());
                if (t.type() == Token.Type.VARIABLE) {
                    sb.append('(').append(t.value()).append(')');
                }
            }
            String actual = sb.toString();
            if (expected.equals(actual)) {
                System.out.println("✓ " + name + ": " + source + " → " + actual);
                passed++;
            } else {
                System.out.println("✗ " + name + ": " + source);
                System.out.println("  期望: " + expected);
                System.out.println("  实际: " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("✗ " + name + ": " + source + " → 异常: " + e.getMessage());
            failed++;
        }
    }

    /**
     * 创建测试上下文
     */
    private static ScriptContext createContext() {
        return new ScriptContext(UUID.randomUUID());
    }

    /**
     * 将 Java 对象转换为 ScriptValue
     */
    private static ScriptValue toScriptValue(Object value) {
        if (value == null) return ScriptValue.nil();
        if (value instanceof String) return ScriptValue.of((String) value);
        if (value instanceof Number) return ScriptValue.of(((Number) value).doubleValue());
        if (value instanceof Boolean) return ScriptValue.of((Boolean) value);
        return ScriptValue.nil();
    }

    /**
     * 根据期望类型转换结果
     */
    private static Object toExpected(ScriptValue result, Object expected) {
        if (expected == null) return null;
        if (expected instanceof Boolean) return result.asBoolean();
        if (expected instanceof Double) return result.asNumber();
        if (expected instanceof String) return result.asString();
        return result;
    }

    /**
     * 执行脚本并返回结果
     */
    private static ScriptValue execute(String source, ScriptContext ctx) {
        List<Token> tokens = new ScriptLexer(source).tokenize();
        ASTNode.BlockNode ast = new ScriptParser(tokens).parse();
        CompiledScript compiled = new ScriptCompiler().compile("test", ast);
        ScriptVM vm = new ScriptVM(compiled, ctx);
        return vm.execute();
    }
}
