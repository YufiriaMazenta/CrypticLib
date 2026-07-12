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
 * Script 功能测试
 * 测试整数类型、变量插值、数学函数等功能
 */
public class ScriptTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        MathScriptModule.INSTANCE.register(ScriptFunctionRegistry.INSTANCE);

        System.out.println("=== Script 功能测试 ===\n");

        // === 整数类型 ===
        System.out.println("--- 整数类型 ---");
        testIntType("整数字面量是Int", "64", true, false);
        testIntType("浮点字面量是Num", "3.14", false, true);
        testIntType("整数值浮点数是Num", "10.0", false, true);
        testIntType("零是Int", "0", true, false);
        testIntType("负整数是Int", "-5", true, false);
        testIntType("大整数是Int", "1000", true, false);

        // === asInt / asLong ===
        System.out.println("\n--- asInt / asLong ---");
        testAsInt("整数字面量asInt", "64", 64);
        testAsInt("负整数asInt", "-5", -5);
        testAsInt("大整数asInt", "1000", 1000);
        testAsInt("浮点数截断为整数", "3.14", 3);
        testAsInt("round结果asInt", "round(3.7)", 4);

        // === 整数运算保持整数 ===
        System.out.println("\n--- 整数运算 ---");
        testIntArith("整数 + 整数 = 整数", "10 + 5", 15L);
        testIntArith("整数 - 整数 = 整数", "10 - 5", 5L);
        testIntArith("整数 * 整数 = 整数", "10 * 5", 50L);
        testIntArith("整数 / 整数 = 整数", "10 / 3", 3L);
        testIntArith("整数 % 整数 = 整数", "10 % 3", 1L);
        testIntArith("负整数取负 = 整数", "-5", -5L);
        testIntArith("裸参数abs整数", "abs -5", 5L);

        // === 混合运算提升为浮点数 ===
        System.out.println("\n--- 混合运算 ---");
        testMixedArith("整数 + 浮点 = 浮点", "10 + 3.14", 13.14);
        testMixedArith("浮点 + 整数 = 浮点", "3.14 + 10", 13.14);
        testMixedArith("整数 * 浮点 = 浮点", "2 * 1.5", 3.0);

        // === 整数比较 ===
        System.out.println("\n--- 整数比较 ---");
        testIntCompare("整数相等", "64 == 64", true);
        testIntCompare("整数不等", "64 != 32", true);
        testIntCompare("整数大于", "64 > 32", true);
        testIntCompare("整数小于", "32 < 64", true);
        testIntCompare("整数大于等于", "64 >= 64", true);
        testIntCompare("整数小于等于", "32 <= 64", true);

        // === Token 类型 ===
        System.out.println("\n--- Token 类型 ---");
        testTokenTypes("整数Token", "64", "INTEGER");
        testTokenTypes("浮点Token", "3.14", "NUMBER");
        testTokenTypes("负整数Token", "-5", "MINUS INTEGER");

        // === 变量引用 ===
        System.out.println("\n--- 变量引用 ---");
        testVarRef("整数变量引用", "${x}", 64L, "x", 64);
        testVarRef("整数变量在表达式中", "${x} > 5", true, "x", 64);
        testVarRef("整数变量算术运算", "${x} + 5", 69L, "x", 64);

        // === 字符串插值 ===
        System.out.println("\n--- 字符串插值 ---");
        testInterpolation("整数插值", "\"count: ${n}\"", "count: 64", "n", 64);
        testInterpolation("浮点插值", "\"damage: ${d}\"", "damage: 3.14", "d", 3.14);
        testInterpolation("浮点数插值", "\"count: ${n}\"", "count: 10.0", "n", 10.0);

        // === 数学函数 ===
        System.out.println("\n--- 数学函数 ---");
        testMath("abs整数", "abs(5)", 5.0);
        testMath("abs负整数", "abs(-5)", 5.0);
        testMath("round四舍五入", "round(3.7)", 4.0);
        testMath("floor向下取整", "floor(3.7)", 3.0);
        testMath("ceil向上取整", "ceil(3.2)", 4.0);

        // === random 函数 ===
        System.out.println("\n--- random 函数 ---");
        testRandomFloat("random无参数", "random()", 0.0, 1.0);
        testRandomFloat("random(10)", "random(10)", 0.0, 10.0);
        testRandomFloat("random(1.5, 10.9)", "random(1.5, 10.9)", 1.5, 10.9);

        // === random_int 函数 ===
        System.out.println("\n--- random_int 函数 ---");
        testRandomInt("random_int(10)", "random_int(10)", 0, 10);
        testRandomInt("random_int(1, 64)", "random_int(1, 64)", 1, 64);

        // === 类型转换函数 ===
        System.out.println("\n--- 类型转换函数 ---");
        testIntType("int(3.14)是整数", "int(3.14)", true, false);
        testAsInt("int(3.14)值为3", "int(3.14)", 3);
        testIntType("int(64)是整数", "int(64)", true, false);
        testIntType("float(64)是浮点", "float(64)", false, true);
        testIntType("float(3.14)是浮点", "float(3.14)", false, true);

        // === 结果 ===
        System.out.println("\n=============================");
        System.out.println("通过: " + passed + "  失败: " + failed);
        System.out.println("=============================");
    }

    /**
     * 测试整数类型判断
     */
    private static void testIntType(String name, String source, boolean expectInt, boolean expectFloat) {
        try {
            ScriptValue result = execute(source, createContext());
            boolean isInt = result.isInteger();
            boolean isFloat = result.isFloat();
            if (isInt == expectInt && isFloat == expectFloat) {
                System.out.println("✓ " + name + ": " + source + " -> " + result);
                passed++;
            } else {
                System.out.println("✗ " + name + ": " + source + " -> " + result
                    + " (isInt=" + isInt + ", isFloat=" + isFloat + ")");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("✗ " + name + ": " + source + " -> 异常: " + e.getMessage());
            failed++;
        }
    }

    /**
     * 测试 asInt 转换
     */
    private static void testAsInt(String name, String source, int expected) {
        try {
            ScriptValue result = execute(source, createContext());
            int actual = result.asInt();
            if (actual == expected) {
                System.out.println("✓ " + name + ": " + source + " -> " + actual);
                passed++;
            } else {
                System.out.println("✗ " + name + ": " + source + " -> 期望 " + expected + ", 实际 " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("✗ " + name + ": " + source + " -> 异常: " + e.getMessage());
            failed++;
        }
    }

    /**
     * 测试整数运算（结果应为 Int 类型）
     */
    private static void testIntArith(String name, String source, long expected) {
        try {
            ScriptValue result = execute(source, createContext());
            if (!result.isInteger()) {
                System.out.println("✗ " + name + ": " + source + " -> 不是整数: " + result);
                failed++;
                return;
            }
            long actual = result.asLong();
            if (actual == expected) {
                System.out.println("✓ " + name + ": " + source + " -> " + actual);
                passed++;
            } else {
                System.out.println("✗ " + name + ": " + source + " -> 期望 " + expected + ", 实际 " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("✗ " + name + ": " + source + " -> 异常: " + e.getMessage());
            failed++;
        }
    }

    /**
     * 测试混合运算（结果应为浮点数）
     */
    private static void testMixedArith(String name, String source, double expected) {
        try {
            ScriptValue result = execute(source, createContext());
            if (!result.isFloat()) {
                System.out.println("✗ " + name + ": " + source + " -> 不是浮点数: " + result);
                failed++;
                return;
            }
            double actual = result.asNumber();
            if (Math.abs(actual - expected) < 0.0001) {
                System.out.println("✓ " + name + ": " + source + " -> " + actual);
                passed++;
            } else {
                System.out.println("✗ " + name + ": " + source + " -> 期望 " + expected + ", 实际 " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("✗ " + name + ": " + source + " -> 异常: " + e.getMessage());
            failed++;
        }
    }

    /**
     * 测试整数比较
     */
    private static void testIntCompare(String name, String source, boolean expected) {
        try {
            ScriptValue result = execute(source, createContext());
            boolean actual = result.asBoolean();
            if (actual == expected) {
                System.out.println("✓ " + name + ": " + source + " -> " + actual);
                passed++;
            } else {
                System.out.println("✗ " + name + ": " + source + " -> 期望 " + expected + ", 实际 " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("✗ " + name + ": " + source + " -> 异常: " + e.getMessage());
            failed++;
        }
    }

    /**
     * 测试变量引用
     */
    private static void testVarRef(String name, String source, Object expected, String varName, Object varValue) {
        try {
            ScriptContext ctx = createContext();
            if (varName != null) {
                ctx.setVariable(varName, toScriptValue(varValue));
            }
            ScriptValue result = execute(source, ctx);
            Object actual;
            if (expected == null) {
                if (result.isNull()) {
                    System.out.println("✓ " + name + ": " + source + " -> nil");
                    passed++;
                    return;
                } else {
                    System.out.println("✗ " + name + ": " + source + " -> 期望 nil, 实际 " + result);
                    failed++;
                    return;
                }
            } else if (expected instanceof Long) {
                actual = result.asLong();
            } else if (expected instanceof Double) {
                actual = result.asNumber();
            } else if (expected instanceof Boolean) {
                actual = result.asBoolean();
            } else {
                actual = result.asString();
            }
            if (expected.equals(actual)) {
                System.out.println("✓ " + name + ": " + source + " -> " + actual);
                passed++;
            } else {
                System.out.println("✗ " + name + ": " + source + " -> 期望 " + expected + ", 实际 " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("✗ " + name + ": " + source + " -> 异常: " + e.getMessage());
            failed++;
        }
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
                System.out.println("✓ " + name + ": " + source + " -> \"" + actual + "\"");
                passed++;
            } else {
                System.out.println("✗ " + name + ": " + source + " -> 期望 \"" + expected + "\", 实际 \"" + actual + "\"");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("✗ " + name + ": " + source + " -> 异常: " + e.getMessage());
            failed++;
        }
    }

    /**
     * 测试数学函数
     */
    private static void testMath(String name, String source, double expected) {
        try {
            ScriptValue result = execute(source, createContext());
            double actual = result.asNumber();
            if (Math.abs(expected - actual) < 0.0001) {
                System.out.println("✓ " + name + ": " + source + " -> " + actual);
                passed++;
            } else {
                System.out.println("✗ " + name + ": " + source + " -> 期望 " + expected + ", 实际 " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("✗ " + name + ": " + source + " -> 异常: " + e.getMessage());
            failed++;
        }
    }

    /**
     * 测试浮点随机数（结果应为浮点且在范围内）
     */
    private static void testRandomFloat(String name, String source, double min, double max) {
        try {
            ScriptValue result = execute(source, createContext());
            if (!result.isFloat()) {
                System.out.println("✗ " + name + ": " + source + " -> 不是浮点数: " + result);
                failed++;
                return;
            }
            double actual = result.asNumber();
            if (actual >= min && actual < max) {
                System.out.println("✓ " + name + ": " + source + " -> " + actual);
                passed++;
            } else {
                System.out.println("✗ " + name + ": " + source + " -> 超出范围 [" + min + ", " + max + "): " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("✗ " + name + ": " + source + " -> 异常: " + e.getMessage());
            failed++;
        }
    }

    /**
     * 测试整数随机数（结果应为整数且在范围内）
     */
    private static void testRandomInt(String name, String source, long min, long max) {
        try {
            ScriptValue result = execute(source, createContext());
            if (!result.isInteger()) {
                System.out.println("✗ " + name + ": " + source + " -> 不是整数: " + result);
                failed++;
                return;
            }
            long actual = result.asLong();
            if (actual >= min && actual < max) {
                System.out.println("✓ " + name + ": " + source + " -> " + actual);
                passed++;
            } else {
                System.out.println("✗ " + name + ": " + source + " -> 超出范围 [" + min + ", " + max + "): " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("✗ " + name + ": " + source + " -> 异常: " + e.getMessage());
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
            }
            String actual = sb.toString();
            if (expected.equals(actual)) {
                System.out.println("✓ " + name + ": " + source + " -> " + actual);
                passed++;
            } else {
                System.out.println("✗ " + name + ": " + source);
                System.out.println("  期望: " + expected);
                System.out.println("  实际: " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("✗ " + name + ": " + source + " -> 异常: " + e.getMessage());
            failed++;
        }
    }

    /**
     * 创建测试上下文
     */
    private static ScriptContext createContext() {
        return new ScriptContext(new ScriptExecutor(null, ScriptExecutor.ExecutorType.CONSOLE));
    }

    /**
     * 将 Java 对象转换为 ScriptValue
     */
    private static ScriptValue toScriptValue(Object value) {
        if (value == null) return ScriptValue.nil();
        if (value instanceof String) return ScriptValue.of((String) value);
        if (value instanceof Long) return ScriptValue.of((Long) value);
        if (value instanceof Integer) return ScriptValue.of((int) value);
        if (value instanceof Double) return ScriptValue.of((Double) value);
        if (value instanceof Boolean) return ScriptValue.of((Boolean) value);
        return ScriptValue.nil();
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
