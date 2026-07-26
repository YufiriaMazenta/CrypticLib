package crypticlib.script;

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

    public ScriptContext(@NotNull Invoker invoker) {
        this.invoker = Objects.requireNonNull(invoker);
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

    @Nullable
    public ScriptValue getVariable(@NotNull String name) {
        return variables.get(name);
    }

    /**
     * 返回变量表的只读视图，防止调用方绕过封装直接修改内部 Map。
     * 如需写入请使用 {@link #setVariable} 或 {@link #setVariables}。
     */
    public Map<String, ScriptValue> variables() {
        return Collections.unmodifiableMap(variables);
    }

}
