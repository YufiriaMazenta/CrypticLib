package crypticlib.script;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * 脚本执行上下文
 * 携带脚本执行时需要的环境信息
 *
 * 设计为通用上下文，数据通过 variables 传入
 */
public class ScriptContext {

    private final @NotNull ScriptExecutor executor;
    private final @NotNull Map<String, ScriptValue> variables = new HashMap<>();

    public ScriptContext(@NotNull ScriptExecutor executor) {
        this.executor = Objects.requireNonNull(executor);
    }

    public @NotNull ScriptExecutor executor() {
        return executor;
    }

    // ---- 变量存取 ----

    public void setVariable(@NotNull String name, @NotNull ScriptValue value) {
        variables.put(name, value);
    }

    @Nullable
    public ScriptValue getVariable(@NotNull String name) {
        return variables.get(name);
    }

    public Map<String, ScriptValue> variables() {
        return variables;
    }

}
