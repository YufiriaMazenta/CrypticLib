package crypticlib.script;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 脚本执行上下文
 * 携带脚本执行时需要的环境信息
 *
 * 设计为通用上下文，业务数据通过 variables 传入
 */
public class ScriptContext {

    private final Player player;
    private final Map<String, ScriptValue> variables = new ConcurrentHashMap<>();

    public ScriptContext(@NotNull Player player) {
        this.player = player;
    }

    @NotNull
    public Player player() {
        return player;
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
