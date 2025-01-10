package crypticlib.action.impl;


import crypticlib.action.Action;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * 空动作
 * 不做任何事情，且会中断动作链的执行
 */
public class EmptyAction implements Action {
    @Override
    public String toActionStr() {
        return null;
    }

    @Override
    public void run(Player player, @NotNull Plugin plugin, @Nullable Function<String, String> argPreprocessor) {

    }

    @Override
    public Action next() {
        return null;
    }

    @Override
    public Action setNext(Action next) {
        return null;
    }

}
