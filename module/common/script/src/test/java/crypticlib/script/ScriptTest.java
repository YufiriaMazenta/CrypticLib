package crypticlib.script;

import crypticlib.CommonPlayer;
import crypticlib.Invoker;
import crypticlib.script.ast.ASTNode;
import crypticlib.script.ast.ScriptParser;
import crypticlib.script.compile.CompiledScript;
import crypticlib.script.compile.ScriptCompiler;
import crypticlib.script.func.MathScriptModule;
import crypticlib.script.func.ScriptFunctionRegistry;
import crypticlib.script.lex.ScriptLexer;
import crypticlib.script.lex.Token;
import crypticlib.script.vm.ScriptVM;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 脚本引擎核心功能测试：词法、语法、编译、虚拟机、类型系统、变量、控制流、模块函数。
 * <p>
 * 语法约定：变量访问为裸标识符（identifier），字符串内插值为 "${name}"，
 * 模块函数调用为 module:function(args)，方法调用为 receiver.method(args)。
 */
public class ScriptTest {

    @BeforeAll
    static void registerModules() {
        MathScriptModule.INSTANCE.register(ScriptFunctionRegistry.INSTANCE);
    }

    // ==================== 整数 / 浮点类型区分 ====================

    @Nested
    @DisplayName("字面量类型")
    class LiteralTypes {

        @Test
        @DisplayName("整数字面量为 Int，浮点字面量为 Num")
        void integerVsFloatLiteral() {
            assertTrue(eval("64").isInteger(), "64 应为整数");
            assertFalse(eval("64").isFloat());

            assertTrue(eval("3.14").isFloat(), "3.14 应为浮点");
            assertFalse(eval("3.14").isInteger());
        }

        @Test
        @DisplayName("整数值浮点字面量 10.0 仍为 Num，不退化为 Int")
        void integralValuedFloatStaysFloat() {
            assertTrue(eval("10.0").isFloat());
            assertFalse(eval("10.0").isInteger());
        }

        @Test
        @DisplayName("零与负整数为 Int")
        void zeroAndNegativeAreInt() {
            assertTrue(eval("0").isInteger());
            assertTrue(eval("-5").isInteger());
            assertTrue(eval("1000").isInteger());
        }
    }

    // ==================== 显式 long 类型 ====================

    @Nested
    @DisplayName("显式 long 类型声明")
    class ExplicitLong {

        @Test
        @DisplayName("普通整数 actualType 为 int.class")
        void plainIntegerIsInt() {
            ScriptValue val = eval("10");
            assertTrue(val.isInteger());
            assertEquals(int.class, val.actualType());
        }

        @Test
        @DisplayName("L 后缀整数 actualType 为 long.class")
        void longSuffixIsLong() {
            ScriptValue val = eval("10L");
            assertTrue(val.isInteger());
            assertEquals(long.class, val.actualType());
        }

        @Test
        @DisplayName("小写 l 后缀也可声明 long")
        void lowercaseLSuffix() {
            ScriptValue val = eval("10l");
            assertTrue(val.isInteger());
            assertEquals(long.class, val.actualType());
        }

        @Test
        @DisplayName("超出 int 范围自动识别为 long")
        void overflowBecomesLong() {
            ScriptValue val = eval("3000000000");
            assertTrue(val.isInteger());
            assertEquals(long.class, val.actualType());
            assertEquals(3000000000L, val.asLong());
        }

        @Test
        @DisplayName("负数 L 后缀")
        void negativeLong() {
            ScriptValue val = eval("-100L");
            assertTrue(val.isInteger());
            assertEquals(long.class, val.actualType());
            assertEquals(-100L, val.asLong());
        }

        @Test
        @DisplayName("Num 类型 actualType 为 double.class")
        void numActualTypeIsDouble() {
            ScriptValue val = eval("3.14");
            assertTrue(val.isFloat());
            assertEquals(double.class, val.actualType());
        }
    }

    // ==================== 数值转换 ====================

    @Nested
    @DisplayName("asInt / asLong")
    class NumericConversion {

        @Test
        @DisplayName("整数保值转换")
        void integerRoundTrip() {
            assertEquals(64, eval("64").asInt());
            assertEquals(-5, eval("-5").asInt());
            assertEquals(1000, eval("1000").asInt());
        }

        @Test
        @DisplayName("浮点转整数为截断而非四舍五入")
        void floatTruncates() {
            assertEquals(3, eval("3.14").asInt());
            assertEquals(3, eval("3.99").asInt());
        }

        @Test
        @DisplayName("round() 是四舍五入，与截断区分")
        void roundIsHalfUp() {
            assertEquals(4, eval("round(3.7)").asInt());
            assertEquals(4, eval("round(3.5)").asInt());
            assertEquals(3, eval("round(3.4)").asInt());
        }
    }

    // ==================== 运算 ====================

    @Nested
    @DisplayName("整数运算保持整数")
    class IntegerArithmetic {

        @Test
        @DisplayName("四则运算与取模结果均为 Int")
        void arithmeticKeepsInt() {
            assertIntResult("10 + 5", 15L);
            assertIntResult("10 - 5", 5L);
            assertIntResult("10 * 5", 50L);
            assertIntResult("10 % 3", 1L);
        }

        @Test
        @DisplayName("整数除法截断取整，不产生浮点")
        void integerDivisionTruncates() {
            assertIntResult("10 / 3", 3L);
            assertIntResult("9 / 3", 3L);
        }

        @Test
        @DisplayName("一元负号保持整数")
        void unaryMinusKeepsInt() {
            assertIntResult("-5", -5L);
        }

        @Test
        @DisplayName("模块函数 math:abs 保持整数")
        void moduleAbsKeepsInt() {
            assertIntResult("math:abs(-5)", 5L);
            assertIntResult("abs(-5)", 5L);
        }
    }

    @Nested
    @DisplayName("混合运算提升为浮点")
    class MixedArithmetic {

        @Test
        @DisplayName("整数与浮点混合运算结果为 Num")
        void mixedPromotesToFloat() {
            assertFloatResult("10 + 3.14", 13.14);
            assertFloatResult("3.14 + 10", 13.14);
            assertFloatResult("2 * 1.5", 3.0);
        }

        @Test
        @DisplayName("除零抛异常而非返回 Infinity")
        void divisionByZeroThrows() {
            assertThrows(ScriptException.class, () -> eval("1 / 0"));
            assertThrows(ScriptException.class, () -> eval("1.0 / 0.0"));
            assertThrows(ScriptException.class, () -> eval("1 % 0"));
        }
    }

    @Nested
    @DisplayName("比较运算")
    class Comparison {

        @Test
        @DisplayName("整数六种比较运算符")
        void integerComparisons() {
            assertTrue(eval("64 == 64").asBoolean());
            assertTrue(eval("64 != 32").asBoolean());
            assertTrue(eval("64 > 32").asBoolean());
            assertTrue(eval("32 < 64").asBoolean());
            assertTrue(eval("64 >= 64").asBoolean());
            assertTrue(eval("32 <= 64").asBoolean());
        }

        @Test
        @DisplayName("非数字字符串与数字比较退回字符串比较，不误判为相等")
        void nonNumericStringVsNumber() {
            // "abc" 不可解析为数字，若走数值比较会因 asBigDecimal 归零而误判 "abc" == 0
            assertFalse(eval("\"abc\" == 0").asBoolean());
        }

        @Test
        @DisplayName("数字字符串与数字比较按数值语义")
        void numericStringVsNumber() {
            assertTrue(eval("\"10\" == 10").asBoolean());
        }
    }

    // ==================== 类型转换的严格性（修复项 3）====================

    @Nested
    @DisplayName("数值转换失败时报错而非静默归零")
    class StrictCoercion {

        @Test
        @DisplayName("非数字字符串参与算术抛异常")
        void nonNumericStringArithmeticThrows() {
            // 修复前 asBigDecimal() 对非数字字符串返回 ZERO，"hello" - 1 会静默得到 -1
            ScriptException e = assertThrows(ScriptException.class, () -> eval("\"hello\" - 1"));
            assertTrue(e.getMessage().contains("hello"), "错误信息应指出无法转换的字符串: " + e.getMessage());
        }

        @Test
        @DisplayName("非数字字符串调用数学函数抛异常")
        void nonNumericStringInMathThrows() {
            // UFCS 让任意值都能 .abs()，此前 "hello".abs() 静默得 0
            assertThrows(ScriptException.class, () -> eval("var s = \"hello\"\ns.abs()"));
            assertThrows(ScriptException.class, () -> eval("math:abs(\"hello\")"));
        }

        @Test
        @DisplayName("数字字符串仍可参与算术")
        void numericStringStillWorks() {
            assertEquals(9L, eval("\"10\" - 1").asLong());
            assertEquals(5L, eval("math:abs(\"-5\")").asLong());
        }

        @Test
        @DisplayName("字符串拼接不受数值转换收紧影响")
        void stringConcatUnaffected() {
            assertEquals("abc1", eval("\"abc\" + 1").asString());
            assertEquals("1abc", eval("1 + \"abc\"").asString());
        }

        @Test
        @DisplayName("布尔值仍可转为数值 1 / 0")
        void booleanCoercionKept() {
            assertEquals(1L, eval("true + 0").asLong());
            assertEquals(0L, eval("false + 0").asLong());
        }
    }

    // ==================== 词法 ====================

    @Nested
    @DisplayName("词法分析")
    class Lexing {

        @Test
        @DisplayName("整数与浮点产生不同 Token 类型")
        void integerAndFloatTokens() {
            assertEquals("INTEGER", firstTokenTypes("64"));
            assertEquals("NUMBER", firstTokenTypes("3.14"));
        }

        @Test
        @DisplayName("负数词法为 MINUS + INTEGER，负号由语法层处理")
        void negativeNumberIsTwoTokens() {
            assertEquals("MINUS INTEGER", firstTokenTypes("-5"));
        }

        @Test
        @DisplayName("单冒号为 COLON，双冒号非法")
        void colonTokens() {
            assertEquals("IDENTIFIER COLON IDENTIFIER", firstTokenTypes("math:abs"));
            assertThrows(ScriptException.class, () -> new ScriptLexer("math::abs(1)").tokenize());
        }

        @Test
        @DisplayName("${} 独立变量引用语法已移除")
        void dollarBraceRemoved() {
            ScriptException e = assertThrows(ScriptException.class, () -> new ScriptLexer("${x}").tokenize());
            assertTrue(e.getMessage().contains("$"), "应报告非法字符 $: " + e.getMessage());
        }

        @Test
        @DisplayName("行注释被跳过，不影响后续 token")
        void lineCommentSkipped() {
            assertEquals(64L, eval("// 这是注释\n64").asLong());
            assertEquals(64L, eval("64 // 尾部注释").asLong());
        }

        @Test
        @DisplayName("连续换行合并为单个 NEWLINE")
        void consecutiveNewlinesCollapse() {
            assertEquals("INTEGER", firstTokenTypes("\n\n\n64\n\n\n"));
        }

        @Test
        @DisplayName("关键字与标识符区分")
        void keywordsVsIdentifiers() {
            assertEquals("VAR IDENTIFIER ASSIGN INTEGER", firstTokenTypes("var x = 1"));
            assertEquals("IF BOOLEAN ENDIF", firstTokenTypes("if true\nendif"));
            assertEquals("RETURN", firstTokenTypes("return"));
            // 关键字前缀的标识符不应被误判：variable 以 var 开头
            assertEquals("IDENTIFIER", firstTokenTypes("variable"));
        }

        @Test
        @DisplayName("下划线与数字可用于标识符")
        void identifierCharset() {
            assertEquals("IDENTIFIER", firstTokenTypes("my_func2"));
            assertEquals("IDENTIFIER", firstTokenTypes("_private"));
        }

        @Test
        @DisplayName("双字符运算符优先于单字符")
        void multiCharOperators() {
            assertEquals("INTEGER EQ INTEGER", firstTokenTypes("1 == 1"));
            assertEquals("INTEGER ASSIGN INTEGER", firstTokenTypes("1 = 1"));
            assertEquals("INTEGER NEQ INTEGER", firstTokenTypes("1 != 1"));
            assertEquals("NOT INTEGER", firstTokenTypes("!1"));
            assertEquals("INTEGER GTE INTEGER", firstTokenTypes("1 >= 1"));
            assertEquals("INTEGER GT INTEGER", firstTokenTypes("1 > 1"));
            assertEquals("BOOLEAN AND BOOLEAN", firstTokenTypes("true && true"));
            assertEquals("BOOLEAN OR BOOLEAN", firstTokenTypes("true || true"));
        }
    }

    // ==================== 字符串 ====================

    @Nested
    @DisplayName("字符串字面量")
    class StringLiterals {

        @Test
        @DisplayName("转义序列 \\n \\t \\\" \\\\ 正确解析")
        void escapeSequences() {
            assertEquals("a\nb", eval("\"a\\nb\"").asString());
            assertEquals("a\tb", eval("\"a\\tb\"").asString());
            assertEquals("a\"b", eval("\"a\\\"b\"").asString());
            assertEquals("a\\b", eval("\"a\\\\b\"").asString());
        }

        @Test
        @DisplayName("\\$ 转义使 ${} 按字面量输出，不触发插值")
        void escapedDollarSuppressesInterpolation() {
            assertEquals("${name}", eval("\"\\${name}\"").asString());
        }

        @Test
        @DisplayName("未识别的转义保留反斜杠")
        void unknownEscapeKeptLiteral() {
            assertEquals("\\q", eval("\"\\q\"").asString());
        }

        @Test
        @DisplayName("未闭合字符串报错")
        void unterminatedStringThrows() {
            ScriptException e = assertThrows(ScriptException.class, () -> eval("\"no end"));
            assertTrue(e.getMessage().contains("Unterminated"), e.getMessage());
        }

        @Test
        @DisplayName("空字符串合法")
        void emptyString() {
            assertEquals("", eval("\"\"").asString());
        }
    }

    // ==================== 变量 ====================

    @Nested
    @DisplayName("变量访问")
    class Variables {

        @Test
        @DisplayName("裸标识符读取上下文变量")
        void bareIdentifierReadsVariable() {
            ScriptContext ctx = ctx();
            ctx.setVariable("x", ScriptValue.of(64));
            assertEquals(64L, exec("x", ctx).asLong());
        }

        @Test
        @DisplayName("变量参与比较与算术")
        void variableInExpression() {
            ScriptContext ctx = ctx();
            ctx.setVariable("x", ScriptValue.of(64));
            assertTrue(exec("x > 5", ctx).asBoolean());
            assertEquals(69L, exec("x + 5", ctx).asLong());
        }

        @Test
        @DisplayName("未声明变量读取抛异常（修复项 2）")
        void undeclaredVariableThrows() {
            // 修复前静默返回 nil，拼写错误无任何提示
            ScriptException e = assertThrows(ScriptException.class, () -> eval("typoVariable"));
            assertTrue(e.getMessage().contains("typoVariable"), "错误信息应含变量名: " + e.getMessage());
        }

        @Test
        @DisplayName("未声明变量参与运算 / 作为实参同样抛异常")
        void undeclaredVariableInUseThrows() {
            assertThrows(ScriptException.class, () -> eval("nope + 1"));
            assertThrows(ScriptException.class, () -> eval("math:abs(nope)"));
            assertThrows(ScriptException.class, () -> eval("nope.get(\"x\")"));
        }

        @Test
        @DisplayName("显式赋为 nil 的变量可读取，与未声明区分")
        void explicitNilIsReadable() {
            ScriptContext ctx = ctx();
            ctx.setVariable("maybe", ScriptValue.nil());
            assertTrue(exec("maybe", ctx).isNull());
        }

        @Test
        @DisplayName("旧式点号模块调用因变量未声明而报错，不再静默算出错值")
        void legacyDotModuleCallThrows() {
            // math.abs(-5) 会先把 math 当变量读取；修复前 math 为 nil，
            // abs(nil, -5) 取 args[0] 得 0，静默返回错误结果
            ScriptException e = assertThrows(ScriptException.class, () -> eval("math.abs(-5)"));
            assertTrue(e.getMessage().contains("math"), "错误信息应指向未声明的 math: " + e.getMessage());
        }
    }

    // ==================== var 声明与赋值 ====================

    @Nested
    @DisplayName("var 声明与赋值")
    class VarDeclaration {

        @Test
        @DisplayName("var 声明并初始化")
        void declareAndInit() {
            ScriptContext ctx = ctx();
            exec("var name = \"Steve\"", ctx);
            assertEquals("Steve", ctx.getVariable("name").asString());
        }

        @Test
        @DisplayName("var 可用表达式初始化")
        void declareWithExpression() {
            ScriptContext ctx = ctx();
            exec("var x = 5\nvar y = x * 2 + 3", ctx);
            assertEquals(13L, ctx.getVariable("y").asLong());
        }

        @Test
        @DisplayName("已声明变量可直接赋值，无需 var")
        void reassignWithoutVar() {
            ScriptContext ctx = ctx();
            exec("var x = 10", ctx);
            exec("x = 20", ctx);
            assertEquals(20L, ctx.getVariable("x").asLong());
        }

        @Test
        @DisplayName("赋值可引用自身，支持累加")
        void selfReferencingAssignment() {
            ScriptContext ctx = ctx();
            exec("var counter = 0", ctx);
            exec("counter = counter + 1\ncounter = counter + 1\ncounter = counter + 1", ctx);
            assertEquals(3L, ctx.getVariable("counter").asLong());
        }

        @Test
        @DisplayName("未声明变量直接赋值抛异常")
        void assignUndeclaredThrows() {
            ScriptContext ctx = ctx();
            ScriptException e = assertThrows(ScriptException.class, () -> exec("undeclared = 100", ctx));
            assertTrue(e.getMessage().contains("undeclared"), e.getMessage());
            assertNull(ctx.getVariable("undeclared"), "报错后不应留下部分写入");
        }

        @Test
        @DisplayName("var 可重复声明并覆盖旧值")
        void redeclareOverwrites() {
            ScriptContext ctx = ctx();
            exec("var x = 1\nvar x = 2", ctx);
            assertEquals(2L, ctx.getVariable("x").asLong());
        }

        @Test
        @DisplayName("var 后缺少变量名或 = 时报错")
        void malformedDeclarationThrows() {
            assertThrows(ScriptException.class, () -> eval("var = 1"));
            assertThrows(ScriptException.class, () -> eval("var x 1"));
            assertThrows(ScriptException.class, () -> eval("var x ="));
            // var 是关键字，不能作变量名
            assertThrows(ScriptException.class, () -> eval("var var = 1"));
        }

        @Test
        @DisplayName("多种类型均可声明")
        void declareVariousTypes() {
            ScriptContext ctx = ctx();
            exec("var s = \"text\"\nvar i = 42\nvar f = 3.14\nvar b = true", ctx);
            assertTrue(ctx.getVariable("s").isString());
            assertTrue(ctx.getVariable("i").isInteger());
            assertTrue(ctx.getVariable("f").isFloat());
            assertTrue(ctx.getVariable("b").isBoolean());
        }
    }

    // ==================== 控制流 ====================

    @Nested
    @DisplayName("控制流")
    class ControlFlow {

        @Test
        @DisplayName("if 条件为真时执行 then 分支")
        void ifTrueBranch() {
            ScriptContext ctx = ctx();
            exec("var r = 0\nif true\nr = 1\nendif", ctx);
            assertEquals(1L, ctx.getVariable("r").asLong());
        }

        @Test
        @DisplayName("if 条件为假时跳过 then 分支")
        void ifFalseSkips() {
            ScriptContext ctx = ctx();
            exec("var r = 0\nif false\nr = 1\nendif", ctx);
            assertEquals(0L, ctx.getVariable("r").asLong());
        }

        @Test
        @DisplayName("else 分支在条件为假时执行")
        void elseBranch() {
            ScriptContext ctx = ctx();
            exec("var r = 0\nif false\nr = 1\nelse\nr = 2\nendif", ctx);
            assertEquals(2L, ctx.getVariable("r").asLong());
        }

        @Test
        @DisplayName("elseif 按顺序求值，命中后不再继续")
        void elseifChain() {
            ScriptContext ctx = ctx();
            exec("var n = 2\nvar r = 0\n"
                + "if n == 1\nr = 10\n"
                + "elseif n == 2\nr = 20\n"
                + "elseif n == 2\nr = 99\n"
                + "else\nr = 30\nendif", ctx);
            assertEquals(20L, ctx.getVariable("r").asLong(), "应命中首个匹配的 elseif");
        }

        @Test
        @DisplayName("elseif 全不命中时走 else")
        void elseifFallsThroughToElse() {
            ScriptContext ctx = ctx();
            exec("var n = 5\nvar r = 0\n"
                + "if n == 1\nr = 10\nelseif n == 2\nr = 20\nelse\nr = 30\nendif", ctx);
            assertEquals(30L, ctx.getVariable("r").asLong());
        }

        @Test
        @DisplayName("嵌套 if 正确配对 endif")
        void nestedIf() {
            ScriptContext ctx = ctx();
            exec("var r = 0\nif true\nif true\nr = 1\nelse\nr = 2\nendif\nendif", ctx);
            assertEquals(1L, ctx.getVariable("r").asLong());
        }

        @Test
        @DisplayName("if 条件按 truthy 语义求值")
        void ifTruthiness() {
            ScriptContext ctx = ctx();
            exec("var r = 0\nif 1\nr = 1\nendif", ctx);
            assertEquals(1L, ctx.getVariable("r").asLong(), "非零整数为真");

            ScriptContext ctx2 = ctx();
            exec("var r = 0\nif 0\nr = 1\nendif", ctx2);
            assertEquals(0L, ctx2.getVariable("r").asLong(), "零为假");
        }

        @Test
        @DisplayName("缺少 endif 时报错")
        void missingEndifThrows() {
            assertThrows(ScriptException.class, () -> eval("if true\nvar x = 1"));
        }

        @Test
        @DisplayName("return 提前结束执行并返回值")
        void returnValue() {
            assertEquals(42L, eval("return 42").asLong());
        }

        @Test
        @DisplayName("return 后的语句不再执行")
        void returnStopsExecution() {
            ScriptContext ctx = ctx();
            exec("var r = 0\nreturn 1\nr = 99", ctx);
            assertEquals(0L, ctx.getVariable("r").asLong(), "return 之后不应继续执行");
        }

        @Test
        @DisplayName("无值 return 返回 nil")
        void bareReturnYieldsNil() {
            assertTrue(eval("return").isNull());
        }

        @Test
        @DisplayName("if 内 return 生效")
        void returnInsideIf() {
            assertEquals(7L, eval("if true\nreturn 7\nendif\nreturn 0").asLong());
        }
    }

    // ==================== 逻辑运算与短路 ====================

    @Nested
    @DisplayName("逻辑运算与短路求值")
    class LogicalOperators {

        @Test
        @DisplayName("&& 与 || 基本真值表")
        void basicTruthTable() {
            assertTrue(eval("true && true").asBoolean());
            assertFalse(eval("true && false").asBoolean());
            assertFalse(eval("false && true").asBoolean());
            assertTrue(eval("true || false").asBoolean());
            assertTrue(eval("false || true").asBoolean());
            assertFalse(eval("false || false").asBoolean());
        }

        @Test
        @DisplayName("! 取反")
        void notOperator() {
            assertFalse(eval("!true").asBoolean());
            assertTrue(eval("!false").asBoolean());
        }

        @Test
        @DisplayName("&& 左侧为假时不求值右侧")
        void andShortCircuits() {
            // 右侧引用未声明变量：若被求值会抛异常，短路则安全
            assertFalse(eval("false && undeclaredOnPurpose").asBoolean());
        }

        @Test
        @DisplayName("|| 左侧为真时不求值右侧")
        void orShortCircuits() {
            assertTrue(eval("true || undeclaredOnPurpose").asBoolean());
        }

        @Test
        @DisplayName("短路不成立时右侧确实被求值")
        void nonShortCircuitEvaluatesRight() {
            assertThrows(ScriptException.class, () -> eval("true && undeclaredOnPurpose"));
            assertThrows(ScriptException.class, () -> eval("false || undeclaredOnPurpose"));
        }
    }

    // ==================== 运算符优先级 ====================

    @Nested
    @DisplayName("运算符优先级与结合性")
    class Precedence {

        @Test
        @DisplayName("乘除优先于加减")
        void multiplyBeforeAdd() {
            assertEquals(14L, eval("2 + 3 * 4").asLong());
            assertEquals(10L, eval("2 * 3 + 4").asLong());
        }

        @Test
        @DisplayName("括号改变优先级")
        void parenthesesOverride() {
            assertEquals(20L, eval("(2 + 3) * 4").asLong());
        }

        @Test
        @DisplayName("加减为左结合")
        void additiveLeftAssociative() {
            assertEquals(1L, eval("10 - 5 - 4").asLong());
        }

        @Test
        @DisplayName("比较优先级低于算术")
        void comparisonAfterArithmetic() {
            assertTrue(eval("2 + 3 == 5").asBoolean());
            assertTrue(eval("2 * 3 > 5").asBoolean());
        }

        @Test
        @DisplayName("&& 优先于 ||")
        void andBeforeOr() {
            // true || (false && false) = true；若按左结合误算 (true||false)&&false 则为 false
            assertTrue(eval("true || false && false").asBoolean());
        }

        @Test
        @DisplayName("一元负号优先于乘法")
        void unaryMinusBindsTighter() {
            assertEquals(-6L, eval("-2 * 3").asLong());
        }

        @Test
        @DisplayName("嵌套过深时报错而非栈溢出")
        void deepNestingThrows() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 300; i++) sb.append('(');
            sb.append('1');
            for (int i = 0; i < 300; i++) sb.append(')');

            ScriptException e = assertThrows(ScriptException.class, () -> eval(sb.toString()));
            assertTrue(e.getMessage().contains("nesting too deep"), e.getMessage());
        }
    }

    // ==================== 字符串插值 ====================

    @Nested
    @DisplayName("字符串插值")
    class Interpolation {

        @Test
        @DisplayName("整数与浮点插值保留原始格式")
        void numericInterpolation() {
            ScriptContext ctx = ctx();
            ctx.setVariable("n", ScriptValue.of(64));
            ctx.setVariable("d", ScriptValue.of(new BigDecimal("3.14")));
            ctx.setVariable("f", ScriptValue.of(new BigDecimal("10.0")));

            assertEquals("count: 64", exec("\"count: ${n}\"", ctx).asString());
            assertEquals("damage: 3.14", exec("\"damage: ${d}\"", ctx).asString());
            assertEquals("count: 10.0", exec("\"count: ${f}\"", ctx).asString());
        }

        @Test
        @DisplayName("相邻插值按字符串拼接，不退化为数值相加")
        void adjacentInterpolationConcatenates() {
            ScriptContext ctx = ctx();
            ctx.setVariable("a", ScriptValue.of(1));
            ctx.setVariable("b", ScriptValue.of(2));
            assertEquals("12", exec("\"${a}${b}\"", ctx).asString());
        }

        @Test
        @DisplayName("插值中未声明变量抛异常")
        void interpolationUndeclaredThrows() {
            assertThrows(ScriptException.class, () -> eval("\"value: ${missing}\""));
        }

        @Test
        @DisplayName("插值语法错误被拦截")
        void malformedInterpolation() {
            assertThrows(ScriptException.class, () -> eval("\"${a-b}\""));
            assertThrows(ScriptException.class, () -> eval("\"${}\""));
        }
    }

    // ==================== 数学函数 ====================

    @Nested
    @DisplayName("数学函数")
    class MathFunctions {

        @Test
        @DisplayName("abs / round / floor / ceil")
        void basicMath() {
            assertEquals(5.0, eval("math:abs(5)").asNumber(), 1e-9);
            assertEquals(5.0, eval("math:abs(-5)").asNumber(), 1e-9);
            assertEquals(4.0, eval("math:round(3.7)").asNumber(), 1e-9);
            assertEquals(3.0, eval("math:floor(3.7)").asNumber(), 1e-9);
            assertEquals(4.0, eval("math:ceil(3.2)").asNumber(), 1e-9);
        }

        @Test
        @DisplayName("floor 与 ceil 对负数方向正确")
        void floorCeilOnNegative() {
            assertEquals(-4.0, eval("math:floor(-3.2)").asNumber(), 1e-9);
            assertEquals(-3.0, eval("math:ceil(-3.2)").asNumber(), 1e-9);
        }

        @Test
        @DisplayName("random() 落在 [0,1) 且为浮点")
        void randomNoArg() {
            for (int i = 0; i < 50; i++) {
                ScriptValue v = eval("math:random()");
                assertTrue(v.isFloat(), "random() 应返回浮点: " + v);
                double d = v.asNumber();
                assertTrue(d >= 0.0 && d < 1.0, "random() 越界: " + d);
            }
        }

        @Test
        @DisplayName("random(max) 与 random(min,max) 落在区间内")
        void randomWithBounds() {
            for (int i = 0; i < 50; i++) {
                double a = eval("math:random(10)").asNumber();
                assertTrue(a >= 0.0 && a < 10.0, "random(10) 越界: " + a);

                double b = eval("math:random(1.5, 10.9)").asNumber();
                assertTrue(b >= 1.5 && b < 10.9, "random(1.5,10.9) 越界: " + b);
            }
        }

        @Test
        @DisplayName("random_int 返回整数且落在区间内")
        void randomInt() {
            for (int i = 0; i < 50; i++) {
                ScriptValue v = eval("math:random_int(10)");
                assertTrue(v.isInteger(), "random_int 应返回整数: " + v);
                long a = v.asLong();
                assertTrue(a >= 0 && a < 10, "random_int(10) 越界: " + a);

                long b = eval("math:random_int(1, 64)").asLong();
                assertTrue(b >= 1 && b < 64, "random_int(1,64) 越界: " + b);
            }
        }

        @Test
        @DisplayName("int() 截断为整数，float() 转为浮点")
        void typeConversionFunctions() {
            assertTrue(eval("math:int(3.14)").isInteger());
            assertEquals(3, eval("math:int(3.14)").asInt());
            assertTrue(eval("math:int(64)").isInteger());

            assertTrue(eval("math:float(64)").isFloat());
            assertTrue(eval("math:float(3.14)").isFloat());
        }

        @Test
        @DisplayName("min / max 保持整数类型，混合时提升为浮点")
        void minMax() {
            assertIntResult("math:min(3, 5)", 3L);
            assertIntResult("math:max(3, 5)", 5L);
            assertTrue(eval("math:min(3, 5.5)").isFloat(), "混合参数应返回浮点");
            assertEquals(5.5, eval("math:max(3, 5.5)").asNumber(), 1e-9);
        }

        @Test
        @DisplayName("pow 幂运算")
        void pow() {
            assertEquals(8.0, eval("math:pow(2, 3)").asNumber(), 1e-9);
            assertEquals(1.0, eval("math:pow(5, 0)").asNumber(), 1e-9);
        }

        @Test
        @DisplayName("sqrt 平方根，负数返回 nil")
        void sqrt() {
            assertEquals(3.0, eval("math:sqrt(9)").asNumber(), 1e-9);
            assertEquals(1.5, eval("math:sqrt(2.25)").asNumber(), 1e-9);
            assertTrue(eval("math:sqrt(-1)").isNull(), "负数无实平方根，应返回 nil");
        }

        @Test
        @DisplayName("round 对整数入参原样返回，不转浮点")
        void roundKeepsIntegerInput() {
            assertIntResult("math:round(5)", 5L);
            assertIntResult("math:floor(5)", 5L);
            assertIntResult("math:ceil(5)", 5L);
        }
    }

    // ==================== 函数注册中心 ====================

    @Nested
    @DisplayName("函数注册中心")
    class Registry {

        @Test
        @DisplayName("同名短名在不同模块注册时判为冲突并移除")
        void conflictingShortNameRemoved() {
            ScriptFunctionRegistry registry = ScriptFunctionRegistry.INSTANCE;
            registry.register("modA", "dup_fn", (c, vm, args) -> ScriptValue.of("A"));
            assertEquals("A", eval("dup_fn()").asString(), "无冲突时短名可用");

            registry.register("modB", "dup_fn", (c, vm, args) -> ScriptValue.of("B"));

            // 冲突后短名失效，必须显式带模块前缀
            assertThrows(ScriptException.class, () -> eval("dup_fn()"));
            assertEquals("A", eval("modA:dup_fn()").asString());
            assertEquals("B", eval("modB:dup_fn()").asString());
        }

        @Test
        @DisplayName("同一 module:function 重复注册为覆盖，不判冲突")
        void sameFullNameOverwrites() {
            ScriptFunctionRegistry registry = ScriptFunctionRegistry.INSTANCE;
            registry.register("modC", "reg_fn", (c, vm, args) -> ScriptValue.of("first"));
            registry.register("modC", "reg_fn", (c, vm, args) -> ScriptValue.of("second"));

            assertEquals("second", eval("modC:reg_fn()").asString());
            assertEquals("second", eval("reg_fn()").asString(), "重复注册同一全名不应使短名失效");
        }

        @Test
        @DisplayName("hasFunction 识别全名与短名")
        void hasFunction() {
            ScriptFunctionRegistry registry = ScriptFunctionRegistry.INSTANCE;
            assertTrue(registry.hasFunction("math.abs"));
            assertTrue(registry.hasFunction("abs"));
            assertFalse(registry.hasFunction("definitelyNotRegistered"));
        }

        @Test
        @DisplayName("注销模块后其函数不可再调用")
        void unregisterModule() {
            ScriptFunctionRegistry registry = ScriptFunctionRegistry.INSTANCE;
            registry.register("modD", "temp_fn", (c, vm, args) -> ScriptValue.of("x"));
            assertEquals("x", eval("modD:temp_fn()").asString());

            registry.unregisterModule("modD");
            assertFalse(registry.hasFunction("modD.temp_fn"));
            assertThrows(ScriptException.class, () -> eval("modD:temp_fn()"));
        }
    }

    // ==================== 模块调用语法（修复项 4）====================

    @Nested
    @DisplayName("模块调用语法")
    class ModuleCallSyntax {

        @Test
        @DisplayName("module:function(args) 为合法形式")
        void colonFormWorks() {
            assertEquals(5L, eval("math:abs(-5)").asLong());
        }

        @Test
        @DisplayName("无冲突时短名可省略模块前缀")
        void shortNameWorks() {
            assertEquals(5L, eval("abs(-5)").asLong());
        }

        @Test
        @DisplayName("冒号形式缺少括号时报错，不退化为带点变量名")
        void colonWithoutParenthesesThrows() {
            // 修复前会造出名为 "math.abs" 的变量引用，脚本无法声明该名字，恒为 nil
            ScriptException e = assertThrows(ScriptException.class, () -> eval("math:abs"));
            assertTrue(e.getMessage().contains("parentheses"),
                "应提示必须带括号: " + e.getMessage());
        }

        @Test
        @DisplayName("冒号后缺少函数名时报错")
        void colonWithoutFunctionNameThrows() {
            assertThrows(ScriptException.class, () -> eval("math:"));
        }

        @Test
        @DisplayName("不存在的模块函数报未知函数")
        void unknownModuleFunctionThrows() {
            ScriptException e = assertThrows(ScriptException.class, () -> eval("nope:whatever(1)"));
            assertTrue(e.getMessage().contains("Unknown function"), e.getMessage());
        }

        @Test
        @DisplayName("自定义模块函数可注册并调用")
        void customModuleFunction() {
            ScriptFunctionRegistry.INSTANCE.register("testmod", "echo",
                (c, vm, args) -> args.length > 0 ? args[0] : ScriptValue.nil());

            assertEquals("hello", eval("testmod:echo(\"hello\")").asString());
            assertEquals("hello", eval("echo(\"hello\")").asString(), "无冲突时短名可用");
        }

        @Test
        @DisplayName("嵌套函数调用按内层先求值")
        void nestedCalls() {
            assertEquals(5L, eval("math:abs(math:min(-5, 3))").asLong());
        }

        @Test
        @DisplayName("函数实参可为任意表达式")
        void argumentsCanBeExpressions() {
            ScriptContext ctx = ctx();
            ctx.setVariable("x", ScriptValue.of(3));
            assertEquals(7L, exec("math:abs(x - 10)", ctx).asLong());
        }

        @Test
        @DisplayName("实参个数不足时函数按自身约定处理")
        void missingArgsHandled() {
            // MathScriptModule 各函数对参数不足统一返回 nil
            assertTrue(eval("math:abs()").isNull());
            assertTrue(eval("math:min(1)").isNull());
        }
    }

    // ==================== 虚拟机 ====================

    @Nested
    @DisplayName("虚拟机执行")
    class VirtualMachine {

        @Test
        @DisplayName("多语句脚本返回最后一个表达式的值")
        void lastExpressionWins() {
            assertEquals(3L, eval("1\n2\n3").asLong());
        }

        @Test
        @DisplayName("空脚本返回 nil")
        void emptyScriptYieldsNil() {
            assertTrue(eval("").isNull());
            assertTrue(eval("\n\n").isNull());
            assertTrue(eval("// 只有注释").isNull());
        }

        @Test
        @DisplayName("超过指令上限时报错，防止无限执行")
        void instructionLimitEnforced() {
            // 用极低上限触发：3 条语句编译后的指令数必然超过 1
            List<Token> tokens = new ScriptLexer("1\n2\n3").tokenize();
            ASTNode.BlockNode ast = new ScriptParser(tokens).parse();
            CompiledScript compiled = new ScriptCompiler().compile("limit-test", ast);

            ScriptVM vm = new ScriptVM(compiled, ctx(), 1);
            ScriptException e = assertThrows(ScriptException.class, vm::execute);
            assertTrue(e.getMessage().contains("maximum instruction limit"), e.getMessage());
        }

        @Test
        @DisplayName("错误信息包含脚本名，便于定位来源")
        void errorMessageCarriesSourceName() {
            List<Token> tokens = new ScriptLexer("missingVariable").tokenize();
            ASTNode.BlockNode ast = new ScriptParser(tokens).parse();
            CompiledScript compiled = new ScriptCompiler().compile("my-script", ast);

            ScriptException e = assertThrows(ScriptException.class,
                () -> new ScriptVM(compiled, ctx()).execute());
            assertTrue(e.getMessage().contains("my-script"), "应含脚本名: " + e.getMessage());
        }

        @Test
        @DisplayName("同一 VM 重复 execute 从干净状态开始")
        void repeatedExecuteResetsState() {
            List<Token> tokens = new ScriptLexer("1 + 1").tokenize();
            ASTNode.BlockNode ast = new ScriptParser(tokens).parse();
            CompiledScript compiled = new ScriptCompiler().compile("test", ast);
            ScriptVM vm = new ScriptVM(compiled, ctx());

            assertEquals(2L, vm.execute().asLong());
            assertEquals(2L, vm.execute().asLong(), "第二次执行结果应相同，不受残留栈影响");
        }

        @Test
        @DisplayName("pause 后 resume 可继续执行")
        void pauseAndResume() {
            ScriptFunctionRegistry.INSTANCE.register("testmod", "pauseHere", (c, vm, args) -> {
                vm.pause();
                return ScriptValue.nil();
            });

            ScriptContext ctx = ctx();
            List<Token> tokens = new ScriptLexer("var r = 1\ntestmod:pauseHere()\nr = 2").tokenize();
            ASTNode.BlockNode ast = new ScriptParser(tokens).parse();
            CompiledScript compiled = new ScriptCompiler().compile("test", ast);
            ScriptVM vm = new ScriptVM(compiled, ctx);

            vm.execute();
            assertTrue(vm.isPaused(), "函数调用 pause() 后 VM 应处于暂停态");
            assertEquals(1L, ctx.getVariable("r").asLong(), "暂停点之后的语句不应已执行");

            vm.resume();
            assertFalse(vm.isPaused());
            assertEquals(2L, ctx.getVariable("r").asLong(), "resume 后应继续执行剩余语句");
        }
    }

    // ==================== 上下文 ====================

    @Nested
    @DisplayName("脚本上下文")
    class Context {

        @Test
        @DisplayName("批量写入变量")
        void setVariablesBulk() {
            ScriptContext ctx = ctx();
            Map<String, ScriptValue> vars = new HashMap<>();
            vars.put("a", ScriptValue.of(1));
            vars.put("b", ScriptValue.of(2));
            ctx.setVariables(vars);

            assertEquals(3L, exec("a + b", ctx).asLong());
        }

        @Test
        @DisplayName("variables() 返回只读视图，防止绕过封装修改")
        void variablesViewIsReadOnly() {
            ScriptContext ctx = ctx();
            ctx.setVariable("a", ScriptValue.of(1));

            assertThrows(UnsupportedOperationException.class,
                () -> ctx.variables().put("b", ScriptValue.of(2)));
        }

        @Test
        @DisplayName("脚本内声明的变量写回上下文，可被宿主读取")
        void scriptVariablesVisibleToHost() {
            ScriptContext ctx = ctx();
            exec("var result = 1 + 2", ctx);
            assertEquals(3L, ctx.getVariable("result").asLong());
        }
    }

    // ==================== 辅助方法 ====================

    private static void assertIntResult(String source, long expected) {
        ScriptValue result = eval(source);
        assertTrue(result.isInteger(), source + " 应返回整数，实际: " + result);
        assertEquals(expected, result.asLong(), source);
    }

    private static void assertFloatResult(String source, double expected) {
        ScriptValue result = eval(source);
        assertTrue(result.isFloat(), source + " 应返回浮点，实际: " + result);
        assertEquals(expected, result.asNumber(), 1e-4, source);
    }

    /** 词法分析后返回各 Token 类型名（不含末尾 EOF），空格分隔 */
    private static String firstTokenTypes(String source) {
        StringBuilder sb = new StringBuilder();
        for (Token t : new ScriptLexer(source).tokenize()) {
            if (t.type() == Token.Type.EOF || t.type() == Token.Type.NEWLINE) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(t.type().name());
        }
        return sb.toString();
    }

    private static ScriptValue eval(String source) {
        return exec(source, ctx());
    }

    private static ScriptValue exec(String source, ScriptContext context) {
        List<Token> tokens = new ScriptLexer(source).tokenize();
        ASTNode.BlockNode ast = new ScriptParser(tokens).parse();
        CompiledScript compiled = new ScriptCompiler().compile("test", ast);
        return new ScriptVM(compiled, context).execute();
    }

    private static ScriptContext ctx() {
        return new ScriptContext(new Invoker() {
            @Override public @NotNull Object platformInvoker() { return this; }
            @Override public @NotNull String name() { return "Console"; }
            @Override public void sendMsg(String msg, Map<String, String> replaceMap) {}
            @Override public boolean hasPermission(String permission) { return true; }
            @Override public boolean isPlayer() { return false; }
            @Override public boolean isConsole() { return true; }
            @Override public CommonPlayer asPlayer() { throw new UnsupportedOperationException(); }
            @Override public InvokerType invokerType() { return InvokerType.CONSOLE; }
        });
    }
}
