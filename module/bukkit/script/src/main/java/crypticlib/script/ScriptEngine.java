package crypticlib.script;

import crypticlib.lifecycle.BukkitLifeCycleTask;
import crypticlib.lifecycle.LifeCycle;
import crypticlib.lifecycle.LifeCycleTaskSettings;
import crypticlib.lifecycle.TaskRule;
import crypticlib.script.ast.ASTNode;
import crypticlib.script.ast.ScriptParser;
import crypticlib.script.compile.CompiledScript;
import crypticlib.script.compile.ScriptCompiler;
import crypticlib.script.func.BuiltinScriptModule;
import crypticlib.script.func.ScriptFunctionRegistry;
import crypticlib.script.func.ScriptModule;
import crypticlib.script.lex.ScriptLexer;
import crypticlib.script.lex.Token;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 脚本引擎
 * 提供脚本的编译、缓存和执行功能
 *
 * 使用方式:
 *   // 注册脚本模块
 *   ScriptEngine.INSTANCE.registerModule(myModule);
 *
 *   // 条件判断
 *   boolean result = ScriptEngine.INSTANCE.evaluate("perm \"my.perm\"", context);
 *
 *   // 动作执行
 *   ScriptEngine.INSTANCE.execute("command \"give %player% diamond\"", context);
 *
 *   // 编译后缓存
 *   CompiledScript script = ScriptEngine.INSTANCE.compile("my_script", source);
 *   script.execute(context);
 */
@LifeCycleTaskSettings(rules = {
    @TaskRule(lifeCycle = LifeCycle.ENABLE),
    @TaskRule(lifeCycle = LifeCycle.RELOAD)
})
public enum ScriptEngine implements BukkitLifeCycleTask {

    INSTANCE;

    /** 编译后的脚本缓存 */
    private final Map<String, CompiledScript> cache = new ConcurrentHashMap<>();

    /** 编译器实例 */
    private final ScriptCompiler compiler = new ScriptCompiler();

    /** 是否已初始化 */
    private boolean initialized = false;

    /**
     * 初始化脚本引擎
     * 使用方应在其生命周期的 ENABLE 阶段调用
     */
    public void init() {
        if (initialized){
            return;
        }
        registerModule(BuiltinScriptModule.INSTANCE);
        initialized = true;
    }

    /**
     * 注册脚本函数模块
     * @param module 函数模块
     */
    public void registerModule(ScriptModule module) {
        module.register(ScriptFunctionRegistry.INSTANCE);
    }

    /**
     * 编译脚本源码
     * @param name 脚本名称（用于缓存和调试）
     * @param source 脚本源码
     * @return 编译后的脚本
     */
    public CompiledScript compile(String name, String source) {
        // 词法分析
        List<Token> tokens = new ScriptLexer(source).tokenize();
        // 语法分析
        ASTNode ast = new ScriptParser(tokens).parse();
        // 编译
        return compiler.compile(name, ast);
    }

    /**
     * 编译并缓存脚本
     * @param name 脚本名称（作为缓存 key）
     * @param source 脚本源码
     * @return 编译后的脚本
     */
    public CompiledScript compileAndCache(String name, String source) {
        CompiledScript script = compile(name, source);
        cache.put(name, script);
        return script;
    }

    /**
     * 从缓存获取编译后的脚本
     * @param name 脚本名称
     * @return 编译后的脚本，不存在返回 null
     */
    public CompiledScript getCached(String name) {
        return cache.get(name);
    }

    /**
     * 获取或编译脚本（缓存优先）
     * @param name 脚本名称
     * @param source 脚本源码（缓存不存在时使用）
     * @return 编译后的脚本
     */
    public CompiledScript getOrCompile(String name, String source) {
        CompiledScript cached = cache.get(name);
        if (cached != null) return cached;
        return compileAndCache(name, source);
    }

    /**
     * 编译并执行脚本（不缓存）
     * @param source 脚本源码
     * @param context 执行上下文
     * @return 执行结果
     */
    public ScriptValue execute(String source, ScriptContext context) {
        CompiledScript script = compile("_inline_" + source.hashCode(), source);
        return script.execute(context);
    }

    /**
     * 编译并执行条件脚本，返回布尔结果（不缓存）
     * @param source 脚本源码
     * @param context 执行上下文
     * @return 条件结果
     */
    public boolean evaluate(String source, ScriptContext context) {
        return execute(source, context).asBoolean();
    }

    /**
     * 清空缓存
     * 使用方应在 reload 时调用
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * 移除指定缓存
     */
    public void removeCached(String name) {
        cache.remove(name);
    }

    @Override
    public void lifecycle(Plugin plugin, LifeCycle lifeCycle) {
        switch (lifeCycle) {
            case ENABLE:
                init();
                break;
            case RELOAD:
                clearCache();
                break;
        }
    }

}
