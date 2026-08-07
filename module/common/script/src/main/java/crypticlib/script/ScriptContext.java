package crypticlib.script;

import crypticlib.CrypticLib;
import crypticlib.Invoker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 脚本执行上下文
 * 携带脚本执行时需要的环境信息
 *
 * 设计为通用上下文，数据通过 variables 传入
 */
public class ScriptContext {

    private final @NotNull Invoker invoker;
    // 使用 ConcurrentHashMap：脚本经 delay 暂停后会在调度器线程恢复并继续读写该 Map，
    // 与创建上下文的插件线程可能并发，普通 HashMap 并发写在扩容时会损坏结构。
    private final @NotNull Map<String, ScriptValue> variables = new ConcurrentHashMap<>();
    /**
     * 此上下文的父母上下文，可从里面读取变量，可能为null
     */
    private final @Nullable ScriptContext parentContext;

    /**
     * 以控制台身份创建一个上下文
     */
    public ScriptContext() {
        this(CrypticLib.plugin().getConsoleInvoker(), null);
    }

    /**
     * 以控制台身份创建上下文，并且传入指定的父母上下文
     * @param parentContext 父母上下文
     */
    public ScriptContext(@Nullable ScriptContext parentContext) {
        this(CrypticLib.plugin().getConsoleInvoker(), parentContext);
    }

    /**
     * 以指定身份创建一个上下文，并且使用空的父母上下文
     * @param invoker 执行者
     */
    public ScriptContext(@NotNull Invoker invoker) {
        this(invoker, null);
    }

    /**
     * 创建一个script上下文
     * @param invoker 此脚本的执行者
     * @param parentContext 父母上下文
     */
    public ScriptContext(@NotNull Invoker invoker, @Nullable ScriptContext parentContext) {
        this.invoker = Objects.requireNonNull(invoker);
        this.parentContext = parentContext;
    }

    public @NotNull Invoker invoker() {
        return invoker;
    }

    // ---- 变量存取 ----

    public void setVariable(@NotNull String name, @NotNull ScriptValue value) {
        variables.put(name, value);
    }

    /**
     * 批量写入变量
     * @param vars 变量表
     */
    public void setVariables(@NotNull Map<String, ScriptValue> vars) {
        variables.putAll(vars);
    }

    /**
     * 从上下文中读取一个变量
     * 如果本上下文中不包含此变量名，那么将会在父母上下文中进行查找
     * @param name 变量名字
     * @return 上下文变量
     */
    @Nullable
    public ScriptValue getVariable(@NotNull String name) {
        ScriptValue scriptValue = variables.get(name);
        if (scriptValue != null) {
            return scriptValue;
        }
        if (parentContext != null) {
            return parentContext.getVariable(name);
        }
        return null;
    }

    /**
     * 返回变量表的只读视图，防止调用方绕过封装直接修改内部 Map。
     * 如需写入请使用 {@link #setVariable} 或 {@link #setVariables}。
     */
    public Map<String, ScriptValue> variables() {
        return Collections.unmodifiableMap(variables);
    }

    public @Nullable ScriptContext parentContext() {
        return parentContext;
    }

}
