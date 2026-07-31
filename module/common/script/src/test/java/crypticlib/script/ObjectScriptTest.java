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
import crypticlib.script.object.ObjectScriptModule;
import crypticlib.script.object.ReflectPropertyResolver;
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
 * Java 对象操作测试：ObjectValue 包装、PropertyResolver 反射读写、
 * 方法调用语法 obj.get/set/invoke、链式访问、错误语义。
 */
public class ObjectScriptTest {

    @BeforeAll
    static void registerModules() {
        MathScriptModule.INSTANCE.register(ScriptFunctionRegistry.INSTANCE);
        ObjectScriptModule.INSTANCE.register(ScriptFunctionRegistry.INSTANCE);
    }

    // ==================== ObjectValue 包装 ====================

    @Nested
    @DisplayName("ObjectValue 包装")
    class Wrapping {

        @Test
        @DisplayName("标量类型拆箱为对应 ScriptValue，不包成 ObjectValue")
        void scalarsUnbox() {
            assertTrue(wrap("hello").isString());
            assertTrue(wrap(42).isInteger());
            assertTrue(wrap(42L).isInteger());
            assertTrue(wrap(3.14).isFloat());
            assertTrue(wrap(true).isBoolean());
        }

        @Test
        @DisplayName("BigDecimal 保留精度，不经 double 中转")
        void bigDecimalKeepsPrecision() {
            BigDecimal precise = new BigDecimal("0.1000000000000000055511151231257827");
            ScriptValue v = wrap(precise);
            assertTrue(v.isFloat());
            assertEquals(precise, v.asBigDecimal());
        }

        @Test
        @DisplayName("复杂对象包成 ObjectValue")
        void complexObjectWraps() {
            ScriptValue v = wrap(new DummyPlayer("Steve"));
            assertTrue(v.isObject());
            assertFalse(v.isString());
            assertFalse(v.isNull());
        }

        @Test
        @DisplayName("null 包成 nil")
        void nullWrapsToNil() {
            assertTrue(wrap(null).isNull());
        }
    }

    // ==================== 反射读属性 ====================

    @Nested
    @DisplayName("属性读取")
    class PropertyRead {

        @Test
        @DisplayName("getXxx() 与 isXxx() 均可识别")
        void beansGetters() {
            assertEquals("Steve", read(new DummyPlayer("Steve"), "name").asString());
            assertTrue(read(new DummyEvent(true, null), "cancelled").asBoolean());
        }

        @Test
        @DisplayName("Map 按 key 读取")
        void mapAccess() {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "hello");
            assertEquals("hello", read(map, "message").asString());
        }

        @Test
        @DisplayName("属性不存在返回 nil，使链式访问可安全传播")
        void missingPropertyReturnsNil() {
            assertTrue(read(new DummyPlayer("Steve"), "nonexistent").isNull());
        }

        @Test
        @DisplayName("裸无参方法不暴露为属性，且不触发副作用")
        void bareMethodNotExposedAsProperty() {
            // 反射若回退到 clazz.getMethod(name)，get("selfDestruct") 会真的执行该方法，
            // 在 Bukkit 上等价于 get("remove") 删实体
            DummyPlayer player = new DummyPlayer("Steve");
            ScriptContext ctx = ctx();
            ctx.setVariable("obj", wrap(player));

            assertTrue(exec("obj.get(\"selfDestruct\")", ctx).isNull());
            assertFalse(player.destroyed, "读属性绝不应触发方法副作用");
        }

        @Test
        @DisplayName("getter 内部抛异常时上抛，不静默变 nil")
        void getterExceptionPropagates() {
            ScriptContext ctx = ctx();
            ctx.setVariable("obj", wrap(new DummyPlayer("Steve")));
            ScriptException e = assertThrows(ScriptException.class, () -> exec("obj.get(\"broken\")", ctx));
            assertTrue(e.getMessage().contains("intentional"), "应保留原始异常信息: " + e.getMessage());
        }
    }

    // ==================== 反射写属性 ====================

    @Nested
    @DisplayName("属性写入")
    class PropertyWrite {

        @Test
        @DisplayName("setter 可写入布尔与字符串")
        void settersWork() {
            DummyEvent event = new DummyEvent(false, null);
            ReflectPropertyResolver.INSTANCE.setProperty(event, "cancelled", ScriptValue.of(true));
            assertTrue(event.isCancelled());

            ReflectPropertyResolver.INSTANCE.setProperty(event, "message", ScriptValue.of("world"));
            assertEquals("world", event.getMessage());
        }

        @Test
        @DisplayName("脚本 set 修改属性")
        void setViaScript() {
            DummyEvent event = new DummyEvent(false, null);
            ScriptContext ctx = ctx();
            ctx.setVariable("event", wrap(event));

            exec("event.set(\"message\", \"new value\")", ctx);
            assertEquals("new value", event.getMessage());
        }

        @Test
        @DisplayName("写不存在的属性抛异常，不静默失败")
        void writeMissingPropertyThrows() {
            // 写操作有副作用，静默失败会让"事件没被取消"这类问题无从排查
            ScriptContext ctx = ctx();
            ctx.setVariable("obj", wrap(new DummyPlayer("Steve")));
            assertThrows(ScriptException.class, () -> exec("obj.set(\"nonexistent\", 1)", ctx));
        }

        @Test
        @DisplayName("写入类型不兼容时抛异常")
        void writeIncompatibleTypeThrows() {
            ScriptContext ctx = ctx();
            ctx.setVariable("obj", wrap(new DummyEvent(false, null)));
            ctx.setVariable("other", wrap(new DummyPlayer("Steve")));
            // message 是 String 字段，传对象无法转换
            assertThrows(ScriptException.class, () -> exec("obj.set(\"player\", \"notAPlayer\")", ctx));
        }
    }

    // ==================== 方法调用 ====================

    @Nested
    @DisplayName("方法调用语法")
    class MethodCallSyntax {

        @Test
        @DisplayName("裸变量可直接调用方法")
        void bareVariableMethodCall() {
            ScriptContext ctx = ctx();
            ctx.setVariable("obj", wrap(new DummyPlayer("Steve")));
            assertEquals("Steve", exec("obj.get(\"name\")", ctx).asString());
        }

        @Test
        @DisplayName("方法调用结果可参与比较")
        void resultInComparison() {
            ScriptContext ctx = ctx();
            ctx.setVariable("obj", wrap(new DummyPlayer("Steve")));
            assertTrue(exec("obj.get(\"name\") == \"Steve\"", ctx).asBoolean());
            assertTrue(exec("obj.get(\"name\") != \"Alex\"", ctx).asBoolean());
        }

        @Test
        @DisplayName("链式访问逐级解包")
        void chainedAccess() {
            ScriptContext ctx = ctx();
            ctx.setVariable("event", wrap(new DummyEvent(false, new DummyPlayer("Alex", 10))));

            assertEquals("Alex", exec("event.get(\"player\").get(\"name\")", ctx).asString());
            assertEquals(10, exec("event.get(\"player\").get(\"level\")", ctx).asInt());
            assertTrue(exec("event.get(\"player\").get(\"name\") == \"Alex\"", ctx).asBoolean());
            assertTrue(exec("event.get(\"cancelled\") == false", ctx).asBoolean());
        }

        @Test
        @DisplayName("变量名与模块名相同时仍解析为方法调用")
        void variableNameShadowingModuleName() {
            // obj 既是变量名也是 ObjectScriptModule 的模块名；
            // 冒号语法把两者分开后，obj.set(...) 必须作用到变量指向的对象
            DummyEvent event = new DummyEvent(false, null);
            ScriptContext ctx = ctx();
            ctx.setVariable("obj", wrap(event));

            exec("obj.set(\"cancelled\", true)", ctx);
            assertTrue(event.isCancelled(), "receiver 不应丢失");
        }

        @Test
        @DisplayName("invoke 调用无参与带参方法")
        void invokeMethods() {
            ScriptContext ctx = ctx();
            ctx.setVariable("obj", wrap(new DummyPlayer("Steve")));

            assertEquals("Steve", exec("obj.invoke(\"getName\")", ctx).asString());
            assertEquals("Hi Steve", exec("obj.invoke(\"greet\", \"Hi\")", ctx).asString());
        }

        @Test
        @DisplayName("invoke void 方法返回 nil 且副作用生效")
        void invokeVoidMethod() {
            DummyPlayer player = new DummyPlayer("Steve");
            ScriptContext ctx = ctx();
            ctx.setVariable("obj", wrap(player));

            assertTrue(exec("obj.invoke(\"setName\", \"Alex\")", ctx).isNull());
            assertEquals("Alex", player.getName());
        }

        @Test
        @DisplayName("链式后接 invoke")
        void chainedThenInvoke() {
            ScriptContext ctx = ctx();
            ctx.setVariable("event", wrap(new DummyEvent(false, new DummyPlayer("Alex"))));
            assertEquals("Alex", exec("event.get(\"player\").invoke(\"getName\")", ctx).asString());
        }

        @Test
        @DisplayName("对象可作为实参传给另一个对象的方法")
        void objectAsArgument() {
            DummyEvent source = new DummyEvent(false, new DummyPlayer("Steve"));
            DummyEvent target = new DummyEvent(false, new DummyPlayer("Alex"));
            ScriptContext ctx = ctx();
            ctx.setVariable("src", wrap(source));
            ctx.setVariable("tgt", wrap(target));

            exec("tgt.invoke(\"setPlayer\", src.get(\"player\"))", ctx);
            assertEquals("Steve", target.getPlayer().getName());
        }
    }

    // ==================== 错误语义 ====================

    @Nested
    @DisplayName("错误语义")
    class ErrorSemantics {

        @Test
        @DisplayName("receiver 为 nil 时链式访问返回 nil 而非报错")
        void nilReceiverPropagates() {
            ScriptContext ctx = ctx();
            ctx.setVariable("obj", ScriptValue.nil());
            assertTrue(exec("obj.get(\"name\")", ctx).isNull());
        }

        @Test
        @DisplayName("中间层属性为 nil 时链式访问返回 nil")
        void nilIntermediatePropagates() {
            ScriptContext ctx = ctx();
            ctx.setVariable("event", wrap(new DummyEvent(false, null)));
            assertTrue(exec("event.get(\"player\").get(\"name\")", ctx).isNull());
        }

        @Test
        @DisplayName("receiver 类型不对时报错")
        void nonObjectReceiverThrows() {
            ScriptException e = assertThrows(ScriptException.class, () -> eval("\"plain\".get(\"name\")"));
            assertTrue(e.getMessage().contains("object receiver"), e.getMessage());
        }

        @Test
        @DisplayName("调用不存在的方法报错")
        void invokeMissingMethodThrows() {
            ScriptContext ctx = ctx();
            ctx.setVariable("obj", wrap(new DummyPlayer("Steve")));
            assertThrows(ScriptException.class, () -> exec("obj.invoke(\"noSuchMethod\")", ctx));
        }

        @Test
        @DisplayName("实参类型不匹配报错，不静默传 null")
        void argTypeMismatchThrows() {
            // convertArg 若返回 null，重载误选时会把 null 静默传进方法
            ScriptContext ctx = ctx();
            ctx.setVariable("obj", wrap(new DummyPlayer("Steve")));
            assertThrows(ScriptException.class, () -> exec("obj.invoke(\"greet\", obj)", ctx));
        }

        @Test
        @DisplayName("方法内部抛异常时保留原因并上抛")
        void invokeExceptionPropagatesCause() {
            ScriptContext ctx = ctx();
            ctx.setVariable("obj", wrap(new DummyPlayer("Steve")));
            ScriptException e = assertThrows(ScriptException.class, () -> exec("obj.invoke(\"boom\")", ctx));
            assertTrue(e.getMessage().contains("intentional"), "应保留原始异常信息: " + e.getMessage());
        }
    }

    // ==================== 对象值的比较与运算 ====================

    @Nested
    @DisplayName("对象值比较与运算")
    class ObjectValueSemantics {

        @Test
        @DisplayName("同一实例相等，不同实例不相等")
        void identityEquality() {
            DummyPlayer steve = new DummyPlayer("Steve");
            DummyPlayer alsoSteve = new DummyPlayer("Steve");

            ScriptContext ctx = ctx();
            ctx.setVariable("a", wrap(steve));
            ctx.setVariable("b", wrap(steve));
            assertTrue(exec("a == b", ctx).asBoolean());
            assertFalse(exec("a != b", ctx).asBoolean());

            ScriptContext ctx2 = ctx();
            ctx2.setVariable("a", wrap(steve));
            ctx2.setVariable("b", wrap(alsoSteve));
            // DummyPlayer 未覆写 equals，字段相同但实例不同即不相等
            assertFalse(exec("a == b", ctx2).asBoolean());
        }

        @Test
        @DisplayName("对象与非对象永不相等")
        void objectNeverEqualsScalar() {
            ScriptContext ctx = ctx();
            ctx.setVariable("a", wrap(new DummyPlayer("Steve")));
            assertFalse(exec("a == \"Steve\"", ctx).asBoolean());
            assertFalse(exec("a == 1", ctx).asBoolean());
        }

        @Test
        @DisplayName("对象参与算术抛异常，不静默归零")
        void objectArithmeticThrows() {
            ScriptContext ctx = ctx();
            ctx.setVariable("obj", wrap(new DummyPlayer("Steve")));
            ScriptException e = assertThrows(ScriptException.class, () -> exec("obj + 1", ctx));
            assertTrue(e.getMessage().contains("DummyPlayer"), "错误信息应含类型名: " + e.getMessage());
        }

        @Test
        @DisplayName("对象在条件判断中按非 null 取真")
        void objectTruthiness() {
            ScriptContext ctx = ctx();
            ctx.setVariable("obj", wrap(new DummyPlayer("Steve")));
            assertTrue(exec("obj", ctx).asBoolean());
        }
    }

    // ==================== 变量与条件脚本 ====================

    @Nested
    @DisplayName("变量与条件脚本")
    class VariablesAndConditions {

        @Test
        @DisplayName("var 声明后可用于方法调用")
        void varDeclarationThenMethodCall() {
            ScriptContext ctx = ctx();
            ctx.setVariable("event", wrap(new DummyEvent(false, new DummyPlayer("Steve"))));

            assertEquals("Steve", exec("var p = event.get(\"player\")\np.get(\"name\")", ctx).asString());
        }

        @Test
        @DisplayName("条件脚本按属性值判定")
        void conditionScripts() {
            ScriptContext notCancelled = ctx();
            notCancelled.setVariable("event", wrap(new DummyEvent(false, null)));
            assertTrue(exec("event.get(\"cancelled\") == false", notCancelled).asBoolean());

            ScriptContext steve = ctx();
            steve.setVariable("event", wrap(new DummyEvent(false, new DummyPlayer("Steve"))));
            assertTrue(exec("event.get(\"player\").get(\"name\") == \"Steve\"", steve).asBoolean());
            assertFalse(exec("event.get(\"player\").get(\"name\") == \"Alex\"", steve).asBoolean());
        }
    }

    // ==================== 辅助方法 ====================

    private static ScriptValue wrap(Object javaObject) {
        return ScriptValue.of(javaObject, ReflectPropertyResolver.INSTANCE);
    }

    private static ScriptValue read(Object target, String property) {
        return ReflectPropertyResolver.INSTANCE.getProperty(target, property);
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

    // ==================== 测试用 POJO ====================

    public static class DummyPlayer {
        private String name;
        private final int level;
        /** 被 get("selfDestruct") 误触发时置 true，用于检测裸方法名泄漏 */
        public boolean destroyed = false;

        public DummyPlayer(String name) { this(name, 10); }
        public DummyPlayer(String name, int level) { this.name = name; this.level = level; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getLevel() { return level; }
        public String greet(String greeting) { return greeting + " " + name; }

        /** 模拟 Bukkit Entity#remove() 这类有副作用的无参方法，绝不应被当作属性读取 */
        public void selfDestruct() { destroyed = true; }

        /** getter 内部抛异常，验证异常上抛而非静默变 nil */
        public String getBroken() { throw new IllegalStateException("intentional getter failure"); }

        /** 方法内部抛异常，验证 invoke 保留原因 */
        public void boom() { throw new IllegalStateException("intentional failure"); }
    }

    public static class DummyEvent {
        private boolean cancelled;
        private DummyPlayer player;
        private String message;

        public DummyEvent(boolean cancelled, DummyPlayer player) { this(cancelled, player, "default"); }
        public DummyEvent(boolean cancelled, DummyPlayer player, String message) {
            this.cancelled = cancelled;
            this.player = player;
            this.message = message;
        }

        public boolean isCancelled() { return cancelled; }
        public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
        public DummyPlayer getPlayer() { return player; }
        public void setPlayer(DummyPlayer player) { this.player = player; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
