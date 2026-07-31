package crypticlib.script;

import crypticlib.CommonPlayer;
import crypticlib.Invoker;
import crypticlib.script.ast.ASTNode;
import crypticlib.script.ast.ScriptParser;
import crypticlib.script.compile.CompiledScript;
import crypticlib.script.compile.ScriptCompiler;
import crypticlib.script.event.ObjectScriptModule;
import crypticlib.script.event.ReflectPropertyResolver;
import crypticlib.script.func.BuiltinScriptModule;
import crypticlib.script.func.MathScriptModule;
import crypticlib.script.func.ScriptFunctionRegistry;
import crypticlib.script.lex.ScriptLexer;
import crypticlib.script.lex.Token;
import crypticlib.script.vm.ScriptVM;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Event object manipulation tests.
 * Tests ObjectValue, PropertyResolver, method-call syntax, chained access.
 */
public class ObjectScriptTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        BuiltinScriptModule.INSTANCE.register(ScriptFunctionRegistry.INSTANCE);
        MathScriptModule.INSTANCE.register(ScriptFunctionRegistry.INSTANCE);
        ObjectScriptModule.INSTANCE.register(ScriptFunctionRegistry.INSTANCE);

        System.out.println("=== Event Script Tests ===\n");

        // ObjectValue basics
        System.out.println("--- ObjectValue ---");
        testObjectValue("String unboxes to Str", "hello", true, false, false);
        testObjectValue("Integer unboxes to Int", 42, false, true, false);
        testObjectValue("Long unboxes to Int", 42L, false, true, false);
        testObjectValue("Double unboxes to Num", 3.14, false, false, true);
        testObjectValue("Boolean unboxes to Bool", true, false, false, false);
        testObjectValue("Complex object wraps to ObjectValue", new DummyPlayer("Steve"), false, false, false);

        // Wrap null vs non-null
        System.out.println("\n--- Wrap null ---");
        testWrapNull("null wraps to nil", null, true);
        testWrapNull("complex object not nil", new DummyPlayer("Alex"), false);

        // ReflectPropertyResolver getter
        System.out.println("\n--- Resolver getter ---");
        testGetProperty("getXxx()", new DummyPlayer("Steve"), "name", "Steve");
        testGetProperty("isXxx()", new DummyEvent(true, null), "cancelled", true);
        testGetProperty("Map.get()", createMapEvent(), "message", "hello");
        testGetProperty("missing property returns nil", new DummyPlayer("Steve"), "nonexistent", null);

        // ReflectPropertyResolver setter
        System.out.println("\n--- Resolver setter ---");
        testSetProperty("set boolean", "cancelled", true);
        testSetProperty("set string", "message", "world");

        // Method-call syntax
        System.out.println("\n--- Method call syntax ---");
        testMethodCall("${obj}.get(\"name\")", "Steve",
            "obj", new DummyPlayer("Steve"));
        testMethodCall("${obj}.get(\"name\") == \"Steve\"", true,
            "obj", new DummyPlayer("Steve"));
        testMethodCall("${obj}.get(\"name\") != \"Alex\"", true,
            "obj", new DummyPlayer("Steve"));

        // Chained method calls
        System.out.println("\n--- Chained method calls ---");
        testMethodCall("${event}.get(\"player\").get(\"name\")", "Alex",
            "event", new DummyEvent(false, new DummyPlayer("Alex")));
        testMethodCall("${event}.get(\"player\").get(\"name\") == \"Alex\"", true,
            "event", new DummyEvent(false, new DummyPlayer("Alex")));
        testMethodCall("${event}.get(\"cancelled\") == false", true,
            "event", new DummyEvent(false, new DummyPlayer("Steve")));
        testMethodCall("${event}.get(\"player\").get(\"level\")", 10,
            "event", new DummyEvent(false, new DummyPlayer("Steve", 10)));

        // set method
        System.out.println("\n--- set method ---");
        testSetViaScript("set modifies property", "message", "new value");

        // invoke
        System.out.println("\n--- invoke ---");
        testCallMethod("call no-arg method", "${obj}.invoke(\"getName\")", "Steve",
            new DummyPlayer("Steve"));
        testCallMethod("call method with arg", "${obj}.invoke(\"greet\", \"Hi\")", "Hi Steve",
            new DummyPlayer("Steve"));
        testCallMethod("call chained then method", "${event}.get(\"player\").invoke(\"getName\")", "Alex",
            new DummyEvent(false, new DummyPlayer("Alex")));
        testCallMethod("call void method", "${obj}.invoke(\"setName\", \"Alex\")", null,
            new DummyPlayer("Steve"));

        // invoke with object arg
        System.out.println("\n--- invoke with object arg ---");
        testCallMethodWithObjectArg("pass object as arg");

        // Null safety
        System.out.println("\n--- Null safety ---");
        testMethodCallNil("nil receiver get returns nil", "obj", null);
        testMethodCallNil("nil player get returns nil", "event", new DummyEvent(false, null));

        // Object equality (compare() object branch)
        System.out.println("\n--- Object equality ---");
        testObjectEquality();

        // Object rejects arithmetic instead of silently yielding 0
        System.out.println("\n--- Object arithmetic rejected ---");
        testObjectArithmeticThrows();

        // Bare no-arg method must NOT be reachable as a property
        System.out.println("\n--- Getter safety ---");
        testBareMethodNotAProperty();

        // Write / invoke failures must be loud
        System.out.println("\n--- Loud failures ---");
        testThrows("set nonexistent property", "${obj}.set(\"nonexistent\", 1)", new DummyPlayer("Steve"));
        testThrows("invoke missing method", "${obj}.invoke(\"noSuchMethod\")", new DummyPlayer("Steve"));
        testThrows("invoke arg type mismatch", "${obj}.invoke(\"greet\", ${obj})", new DummyPlayer("Steve"));
        testThrows("get on non-object receiver", "\"plain\".get(\"name\")", null);
        testInvokeRuntimeExceptionPropagates();

        // Builtin set_var rename
        System.out.println("\n--- var assignment ---");
        testVarAssignment();

        // Condition scripts
        System.out.println("\n--- Condition scripts ---");
        testConditionScript("event not cancelled",
            "${event}.get(\"cancelled\") == false", true,
            "event", new DummyEvent(false, null));
        testConditionScript("player name matches",
            "${event}.get(\"player\").get(\"name\") == \"Steve\"", true,
            "event", new DummyEvent(false, new DummyPlayer("Steve")));
        testConditionScript("player name mismatch",
            "${event}.get(\"player\").get(\"name\") == \"Alex\"", false,
            "event", new DummyEvent(false, new DummyPlayer("Steve")));

        // Result
        System.out.println("\n=============================");
        System.out.println("Passed: " + passed + "  Failed: " + failed);
        System.out.println("=============================");
    }

    // ==================== Test methods ====================

    private static void testObjectValue(String name, Object input, boolean expectStr, boolean expectInt, boolean expectFloat) {
        try {
            ScriptValue val = ScriptValue.of(input, ReflectPropertyResolver.INSTANCE);
            boolean isStr = val.isString();
            boolean isInt = val.isInteger();
            boolean isFloat = val.isFloat();
            boolean isObj = val.isObject();
            boolean isBool = val.isBoolean();
            boolean isNil = val.isNull();

            if (isStr == expectStr && isInt == expectInt && isFloat == expectFloat) {
                System.out.println("[PASS] " + name + ": " + val + " (str=" + isStr + " int=" + isInt + " float=" + isFloat + " obj=" + isObj + " bool=" + isBool + " nil=" + isNil + ")");
                passed++;
            } else {
                System.out.println("[FAIL] " + name + ": isStr=" + isStr + ", isInt=" + isInt + ", isFloat=" + isFloat);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("[FAIL] " + name + " -> Exception: " + e.getMessage());
            failed++;
        }
    }

    private static void testWrapNull(String name, Object input, boolean expectNull) {
        try {
            ScriptValue val = ScriptValue.of(input, ReflectPropertyResolver.INSTANCE);
            if (val.isNull() == expectNull) {
                System.out.println("[PASS] " + name + ": " + val);
                passed++;
            } else {
                System.out.println("[FAIL] " + name + ": expected null=" + expectNull + ", got " + val);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("[FAIL] " + name + " -> Exception: " + e.getMessage());
            failed++;
        }
    }

    private static void testGetProperty(String name, Object target, String property, Object expected) {
        try {
            ScriptValue val = ReflectPropertyResolver.INSTANCE.getProperty(target, property);
            if (expected == null) {
                if (val.isNull()) {
                    System.out.println("[PASS] " + name + ": " + property + " -> nil");
                    passed++;
                } else {
                    System.out.println("[FAIL] " + name + ": expected nil, got " + val);
                    failed++;
                }
                return;
            }
            Object actual;
            if (expected instanceof String) actual = val.asString();
            else if (expected instanceof Boolean) actual = val.asBoolean();
            else if (expected instanceof Integer) actual = val.asInt();
            else actual = val.asString();

            if (expected.equals(actual)) {
                System.out.println("[PASS] " + name + ": " + property + " -> " + actual);
                passed++;
            } else {
                System.out.println("[FAIL] " + name + ": expected " + expected + ", got " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("[FAIL] " + name + " -> Exception: " + e.getMessage());
            failed++;
        }
    }

    private static void testSetProperty(String name, String property, Object value) {
        try {
            DummyEvent event = new DummyEvent(false, null);
            ScriptValue scriptVal = (value instanceof Boolean)
                ? ScriptValue.of((boolean) value)
                : ScriptValue.of((String) value);
            ReflectPropertyResolver.INSTANCE.setProperty(event, property, scriptVal);
            ScriptValue result = ReflectPropertyResolver.INSTANCE.getProperty(event, property);
            Object actual = (value instanceof Boolean) ? result.asBoolean() : result.asString();
            if (value.equals(actual)) {
                System.out.println("[PASS] " + name + ": set " + property + " = " + value);
                passed++;
            } else {
                System.out.println("[FAIL] " + name + ": expected " + value + ", got " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("[FAIL] " + name + " -> Exception: " + e.getMessage());
            failed++;
        }
    }

    private static void testMethodCall(String source, Object expected, String varName, Object varValue) {
        try {
            ScriptContext ctx = createContext();
            if (varName != null) {
                if (varValue != null) {
                    ctx.setVariable(varName, ScriptValue.of(varValue, ReflectPropertyResolver.INSTANCE));
                } else {
                    ctx.setVariable(varName, ScriptValue.nil());
                }
            }
            ScriptValue result = execute(source, ctx);

            if (expected == null) {
                if (result.isNull()) {
                    System.out.println("[PASS] " + source + " -> nil");
                    passed++;
                } else {
                    System.out.println("[FAIL] " + source + " -> expected nil, got " + result);
                    failed++;
                }
                return;
            }

            Object actual;
            if (expected instanceof String) actual = result.asString();
            else if (expected instanceof Boolean) actual = result.asBoolean();
            else if (expected instanceof Integer) actual = result.asInt();
            else if (expected instanceof Long) actual = result.asLong();
            else actual = result.asString();

            if (expected.equals(actual)) {
                System.out.println("[PASS] " + source + " -> " + actual);
                passed++;
            } else {
                System.out.println("[FAIL] " + source + " -> expected " + expected + ", got " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("[FAIL] " + source + " -> Exception: " + e.getMessage());
            failed++;
        }
    }

    private static void testSetViaScript(String name, String property, Object expected) {
        try {
            DummyEvent event = new DummyEvent(false, null);
            ScriptContext ctx = createContext();
            ctx.setVariable("event", ScriptValue.of(event, ReflectPropertyResolver.INSTANCE));
            String script = "${event}.set(\"" + property + "\", \"" + expected + "\")";
            execute(script, ctx);
            ScriptValue result = ReflectPropertyResolver.INSTANCE.getProperty(event, property);
            String actual = result.asString();
            if (expected.equals(actual)) {
                System.out.println("[PASS] " + name + ": " + property + " = " + actual);
                passed++;
            } else {
                System.out.println("[FAIL] " + name + ": expected " + expected + ", got " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("[FAIL] " + name + " -> Exception: " + e.getMessage());
            failed++;
        }
    }

    private static void testCallMethod(String name, String source, Object expected, Object varValue) {
        try {
            ScriptContext ctx = createContext();
            ctx.setVariable("obj", ScriptValue.of(varValue, ReflectPropertyResolver.INSTANCE));
            ctx.setVariable("event", ScriptValue.of(varValue, ReflectPropertyResolver.INSTANCE));
            ScriptValue result = execute(source, ctx);
            if (expected == null) {
                if (result.isNull()) {
                    System.out.println("[PASS] " + name + " -> nil");
                    passed++;
                } else {
                    System.out.println("[FAIL] " + name + " -> expected nil, got " + result);
                    failed++;
                }
                return;
            }
            Object actual;
            if (expected instanceof String) actual = result.asString();
            else if (expected instanceof Boolean) actual = result.asBoolean();
            else if (expected instanceof Integer) actual = result.asInt();
            else actual = result.asString();
            if (expected.equals(actual)) {
                System.out.println("[PASS] " + name + " -> " + actual);
                passed++;
            } else {
                System.out.println("[FAIL] " + name + " -> expected " + expected + ", got " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("[FAIL] " + name + " -> Exception: " + e.getMessage());
            failed++;
        }
    }

    private static void testConditionScript(String name, String condition, boolean expected, String varName, Object varValue) {
        try {
            ScriptContext ctx = createContext();
            if (varName != null && varValue != null) {
                ctx.setVariable(varName, ScriptValue.of(varValue, ReflectPropertyResolver.INSTANCE));
            }
            ScriptValue result = execute(condition, ctx);
            boolean actual = result.asBoolean();
            if (actual == expected) {
                System.out.println("[PASS] " + name + ": " + condition + " -> " + actual);
                passed++;
            } else {
                System.out.println("[FAIL] " + name + ": expected " + expected + ", got " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("[FAIL] " + name + " -> Exception: " + e.getMessage());
            failed++;
        }
    }

    private static void testMethodCallNil(String name, String varName, Object varValue) {
        try {
            ScriptContext ctx = createContext();
            if (varName != null) {
                if (varValue != null) {
                    ctx.setVariable(varName, ScriptValue.of(varValue, ReflectPropertyResolver.INSTANCE));
                } else {
                    ctx.setVariable(varName, ScriptValue.nil());
                }
            }
            String source = "${" + varName + "}.get(\"name\")";
            ScriptValue result = execute(source, ctx);
            if (result.isNull()) {
                System.out.println("[PASS] " + name + " -> nil");
                passed++;
            } else {
                System.out.println("[FAIL] " + name + " -> expected nil, got " + result);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("[FAIL] " + name + " -> Exception: " + e.getMessage());
            failed++;
        }
    }

    private static void testCallMethodWithObjectArg(String name) {
        try {
            // Scenario: get player from event, then set it on another event via invoke
            DummyEvent sourceEvent = new DummyEvent(false, new DummyPlayer("Steve"));
            DummyEvent targetEvent = new DummyEvent(false, new DummyPlayer("Alex"));
            ScriptContext ctx = createContext();
            ctx.setVariable("src", ScriptValue.of(sourceEvent, ReflectPropertyResolver.INSTANCE));
            ctx.setVariable("tgt", ScriptValue.of(targetEvent, ReflectPropertyResolver.INSTANCE));

            // src.get("player") returns ObjectValue(Player), pass it to tgt.setPlayer(...)
            execute("${tgt}.invoke(\"setPlayer\", ${src}.get(\"player\"))", ctx);

            // targetEvent's player should now be Steve
            String actual = targetEvent.getPlayer().getName();
            if ("Steve".equals(actual)) {
                System.out.println("[PASS] " + name + ": tgt.player.name = " + actual);
                passed++;
            } else {
                System.out.println("[FAIL] " + name + ": expected Steve, got " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("[FAIL] " + name + " -> Exception: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }
    }

    /**
     * 验证 compare() 的对象分支：同一实例相等，不同实例不相等，对象与非对象永不相等。
     * 修复前两个 ObjectValue 相比会退化为 toString() 字典序。
     */
    private static void testObjectEquality() {
        DummyPlayer steve = new DummyPlayer("Steve");
        DummyPlayer alsoSteve = new DummyPlayer("Steve");
        checkBool("same instance ==", "${a} == ${b}", true, steve, steve);
        checkBool("same instance !=", "${a} != ${b}", false, steve, steve);
        // 不同实例即使字段相同也不相等（DummyPlayer 未覆写 equals）
        checkBool("different instance ==", "${a} == ${b}", false, steve, alsoSteve);
        checkBool("object vs string ==", "${a} == \"Steve\"", false, steve, steve);
    }

    private static void checkBool(String name, String source, boolean expected, Object a, Object b) {
        try {
            ScriptContext ctx = createContext();
            ctx.setVariable("a", ScriptValue.of(a, ReflectPropertyResolver.INSTANCE));
            ctx.setVariable("b", ScriptValue.of(b, ReflectPropertyResolver.INSTANCE));
            boolean actual = execute(source, ctx).asBoolean();
            if (actual == expected) {
                System.out.println("[PASS] " + name + ": " + source + " -> " + actual);
                passed++;
            } else {
                System.out.println("[FAIL] " + name + ": expected " + expected + ", got " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("[FAIL] " + name + " -> Exception: " + e.getMessage());
            failed++;
        }
    }

    /**
     * 验证对象不能参与数值运算。修复前 ObjectValue 未覆写 asBigDecimal()，
     * 父类 fallback 用 toString() 解析失败后返回 ZERO，${obj} + 1 静默得到 1。
     */
    private static void testObjectArithmeticThrows() {
        ScriptContext ctx = createContext();
        ctx.setVariable("obj", ScriptValue.of(new DummyPlayer("Steve"), ReflectPropertyResolver.INSTANCE));
        try {
            ScriptValue result = execute("${obj} + 1", ctx);
            System.out.println("[FAIL] object arithmetic should throw, got " + result);
            failed++;
        } catch (ScriptException e) {
            System.out.println("[PASS] object arithmetic throws: " + e.getMessage());
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] expected ScriptException, got " + e.getClass().getSimpleName() + ": " + e.getMessage());
            failed++;
        }
    }

    /**
     * 验证裸方法名不再被当作属性读取。修复前 findGetter 回退到 clazz.getMethod(name)，
     * get("selfDestruct") 会真的执行该方法 —— 在 Bukkit 上等价于 get("remove") 删实体。
     */
    private static void testBareMethodNotAProperty() {
        DummyPlayer player = new DummyPlayer("Steve");
        ScriptContext ctx = createContext();
        ctx.setVariable("obj", ScriptValue.of(player, ReflectPropertyResolver.INSTANCE));
        try {
            ScriptValue result = execute("${obj}.get(\"selfDestruct\")", ctx);
            boolean sideEffectHappened = player.destroyed;
            if (result.isNull() && !sideEffectHappened) {
                System.out.println("[PASS] bare method not exposed as property -> nil, no side effect");
                passed++;
            } else {
                System.out.println("[FAIL] bare method leak: result=" + result + ", destroyed=" + sideEffectHappened);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("[FAIL] bare method probe -> Exception: " + e.getMessage());
            failed++;
        }
    }

    /**
     * 验证方法内部抛出的异常会上抛而非被吞掉。
     * 修复前 catch (Exception ignored) 会让 setCancelled 之类的失败完全静默。
     */
    private static void testInvokeRuntimeExceptionPropagates() {
        ScriptContext ctx = createContext();
        ctx.setVariable("obj", ScriptValue.of(new DummyPlayer("Steve"), ReflectPropertyResolver.INSTANCE));
        try {
            execute("${obj}.invoke(\"boom\")", ctx);
            System.out.println("[FAIL] invoke of throwing method should propagate, but returned normally");
            failed++;
        } catch (ScriptException e) {
            boolean mentionsCause = e.getMessage() != null && e.getMessage().contains("intentional");
            if (mentionsCause) {
                System.out.println("[PASS] invoke propagates cause: " + e.getMessage());
                passed++;
            } else {
                System.out.println("[FAIL] ScriptException lost original cause: " + e.getMessage());
                failed++;
            }
        } catch (Exception e) {
            System.out.println("[FAIL] expected ScriptException, got " + e.getClass().getSimpleName());
            failed++;
        }
    }

    /** 断言脚本执行抛出 ScriptException；varValue 为 null 时不注入 obj 变量 */
    private static void testThrows(String name, String source, Object varValue) {
        ScriptContext ctx = createContext();
        if (varValue != null) {
            ctx.setVariable("obj", ScriptValue.of(varValue, ReflectPropertyResolver.INSTANCE));
        }
        try {
            ScriptValue result = execute(source, ctx);
            System.out.println("[FAIL] " + name + ": expected ScriptException, got " + result);
            failed++;
        } catch (ScriptException e) {
            System.out.println("[PASS] " + name + " throws: " + e.getMessage());
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] " + name + ": expected ScriptException, got "
                + e.getClass().getSimpleName() + ": " + e.getMessage());
            failed++;
        }
    }

    /**
     * 验证 var 语法可用于变量赋值，且 obj.set 的短名可用。
     */
    private static void testVarAssignment() {
        try {
            ScriptContext ctx = createContext();
            execute("var k = 42", ctx);
            ScriptValue got = execute("${k}", ctx);
            if (got.asLong() == 42L) {
                System.out.println("[PASS] var assignment + ${k} -> " + got.asLong());
                passed++;
            } else {
                System.out.println("[FAIL] var assignment: expected 42, got " + got);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("[FAIL] var assignment -> Exception: " + e.getMessage());
            failed++;
        }

        // obj.set 的短名必须可用（若被 builtin set 占用会判冲突而移除）
        try {
            DummyEvent event = new DummyEvent(false, null);
            ScriptContext ctx = createContext();
            ctx.setVariable("event", ScriptValue.of(event, ReflectPropertyResolver.INSTANCE));
            execute("${event}.set(\"cancelled\", true)", ctx);
            if (event.isCancelled()) {
                System.out.println("[PASS] obj.set short name resolves after builtin rename");
                passed++;
            } else {
                System.out.println("[FAIL] obj.set did not take effect");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("[FAIL] obj.set short name -> Exception: " + e.getMessage());
            failed++;
        }
    }

    // ==================== Helpers ====================

    private static ScriptContext createContext() {
        return new ScriptContext(new Invoker() {
            @Override public @NotNull Object platformInvoker() { return null; }
            @Override public @NotNull String name() { return "Console"; }
            @Override public void sendMsg(String msg, Map<String, String> replaceMap) {}
            @Override public boolean hasPermission(String permission) { return true; }
            @Override public boolean isPlayer() { return false; }
            @Override public boolean isConsole() { return true; }
            @Override public CommonPlayer asPlayer() { throw new UnsupportedOperationException(); }
            @Override public InvokerType invokerType() { return InvokerType.CONSOLE; }
        });
    }

    private static ScriptValue execute(String source, ScriptContext ctx) {
        List<Token> tokens = new ScriptLexer(source).tokenize();
        ASTNode.BlockNode ast = new ScriptParser(tokens).parse();
        CompiledScript compiled = new ScriptCompiler().compile("test", ast);
        ScriptVM vm = new ScriptVM(compiled, ctx);
        return vm.execute();
    }

    private static Object createMapEvent() {
        Map<String, Object> map = new HashMap<>();
        map.put("message", "hello");
        return map;
    }

    // ==================== Test POJOs ====================

    public static class DummyPlayer {
        private String name;
        private final int level;
        /** 被 get("selfDestruct") 误触发时会置 true，用于检测裸方法名泄漏 */
        public boolean destroyed = false;

        public DummyPlayer(String name) { this(name, 10); }
        public DummyPlayer(String name, int level) { this.name = name; this.level = level; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getLevel() { return level; }
        public String greet(String greeting) { return greeting + " " + name; }

        /** 模拟 Bukkit Entity#remove() 这类有副作用的无参方法，绝不应被当作属性读取 */
        public void selfDestruct() { destroyed = true; }

        /** 模拟方法内部抛异常，验证异常会上抛而非被吞掉 */
        public void boom() { throw new IllegalStateException("intentional failure"); }
    }

    public static class DummyEvent {
        private boolean cancelled;
        private DummyPlayer player;
        private String message;

        public DummyEvent(boolean cancelled, DummyPlayer player) { this(cancelled, player, "default"); }
        public DummyEvent(boolean cancelled, DummyPlayer player, String message) {
            this.cancelled = cancelled; this.player = player; this.message = message;
        }
        public boolean isCancelled() { return cancelled; }
        public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
        public DummyPlayer getPlayer() { return player; }
        public void setPlayer(DummyPlayer player) { this.player = player; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String describePlayer() { return player == null ? "nobody" : player.getName(); }
    }
}
