package crypticlib.action;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * 动作接口
 * 若动作能够执行下一个动作，需要在run中手动调用runNext方法
 */
public interface Action {

    void run(Player player, Plugin plugin);

    Action next();

    default void runNext(Player player, Plugin plugin) {
        if (next() != null)
            next().run(player, plugin);
    }

    Action setNext(Action next);

}
