package crypticlib.script;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 脚本执行上下文
 * 携带脚本执行时需要的环境信息
 *
 * 设计为通用上下文，业务数据通过 variables 传入
 */
public class ScriptContext {

    private final UUID playerId;
    private final Map<String, ScriptValue> variables = new ConcurrentHashMap<>();

    public ScriptContext(@NotNull UUID playerId) {
        this.playerId = playerId;
    }

    @NotNull
    public UUID playerId() {
        return playerId;
    }

    /**
     * 获取在线玩家实例
     * 如果玩家不在线则返回null
     */
    public Optional<Player> onlinePlayerOpt() {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return Optional.empty();
        }
        if (!player.isOnline()) {
            return Optional.empty();
        }
        return Optional.of(player);
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
