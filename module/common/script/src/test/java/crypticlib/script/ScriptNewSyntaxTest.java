package crypticlib.script;

import crypticlib.CommonPlayer;
import crypticlib.Invoker;
import crypticlib.script.ast.ASTNode;
import crypticlib.script.ast.ScriptParser;
import crypticlib.script.compile.CompiledScript;
import crypticlib.script.compile.ScriptCompiler;
import crypticlib.script.func.ScriptFunctionRegistry;
import crypticlib.script.lex.ScriptLexer;
import crypticlib.script.lex.Token;
import crypticlib.script.vm.ScriptVM;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试新的脚本语法特性
 */
public class ScriptNewSyntaxTest {

    // 不调用 ScriptEngine.init()，避免触发 CrypticLib 静态初始化
    // 测试直接使用 ScriptLexer/ScriptParser/ScriptCompiler/ScriptVM

    @Test
    public void testVarDeclaration() {
        ScriptContext ctx = createContext();
        execute("var name = \"Steve\"", ctx);
        assertEquals("Steve", ctx.getVariable("name").asString());
    }

    @Test
    public void testDirectAssignment() {
        ScriptContext ctx = createContext();
        execute("var x = 10", ctx);
        execute("x = 20", ctx);
        assertEquals(20L, ctx.getVariable("x").asLong());
    }

    @Test
    public void testReassignUndeclaredShouldFail() {
        ScriptContext ctx = createContext();
        assertThrows(ScriptException.class, () -> {
            execute("undeclared = 100", ctx);
        });
    }

    @Test
    public void testDirectVarAccess() {
        ScriptContext ctx = createContext();
        execute("var greeting = \"Hello\"", ctx);
        ScriptValue val = execute("greeting", ctx);
        assertEquals("Hello", val.asString());
    }

    @Test
    public void testStringInterpolation() {
        ScriptContext ctx = createContext();
        execute("var name = \"Steve\"", ctx);
        ScriptValue val = execute("\"Hello ${name}\"", ctx);
        assertEquals("Hello Steve", val.asString());
    }

    @Test
    public void testFunctionCall() {
        ScriptFunctionRegistry.INSTANCE.register("test", "echo", (c, vm, args) -> {
            if (args.length > 0) return args[0];
            return ScriptValue.nil();
        });

        ScriptContext ctx = createContext();
        ScriptValue val = execute("test:echo(\"hello\")", ctx);
        assertEquals("hello", val.asString());
    }

    @Test
    public void testMixedSyntax() {
        ScriptContext ctx = createContext();
        execute("var a = 10", ctx);
        execute("var b = 20", ctx);
        execute("var c = a + b", ctx);
        assertEquals(30L, ctx.getVariable("c").asLong());
    }

    @Test
    public void testVarWithExpression() {
        ScriptContext ctx = createContext();
        execute("var x = 5", ctx);
        execute("var y = x * 2 + 3", ctx);
        assertEquals(13L, ctx.getVariable("y").asLong());
    }

    @Test
    public void testDirectAssignmentChain() {
        ScriptContext ctx = createContext();
        execute("var counter = 0", ctx);
        execute("counter = counter + 1", ctx);
        execute("counter = counter + 1", ctx);
        execute("counter = counter + 1", ctx);
        assertEquals(3L, ctx.getVariable("counter").asLong());
    }

    @Test
    public void testVarWithObjectAccess() {
        ScriptContext ctx = createContext();
        execute("var x = 10", ctx);
        ScriptValue val = execute("x", ctx);
        assertEquals(10L, val.asLong());
    }

    // ========== 辅助方法 ==========

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
}
