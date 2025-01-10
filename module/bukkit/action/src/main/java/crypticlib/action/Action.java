package crypticlib.action;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 动作接口
 * 若动作能够执行下一个动作，需要在run中手动调用runNext方法
 */
public interface Action {

    /**
     * 将动作还原为动作语句,实现者需保证还原出的语句能重新编译回此动作
     * @return
     */
    String toActionStr();

    void run(@Nullable Player player, @NotNull Plugin plugin, @Nullable Map<String, String> args);

    Action next();

    default void runNext(@Nullable Player player, @NotNull Plugin plugin, @Nullable Map<String, String> args) {
        if (next() != null)
            next().run(player, plugin, args);
    }

    Action setNext(Action next);

}
