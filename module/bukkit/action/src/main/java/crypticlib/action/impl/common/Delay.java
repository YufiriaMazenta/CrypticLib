package crypticlib.action.impl.common;

import crypticlib.CrypticLibBukkit;
import crypticlib.action.BaseAction;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public class Delay extends BaseAction {

    private Integer delayTick;
    private final String delayTickStr;

    public Delay(String delayTickStr) {
        this.delayTickStr = delayTickStr;
        if (delayTickStr == null || delayTickStr.isEmpty()) {
            delayTick = 0;
            return;
        }
        try {
            delayTick = Integer.parseInt(delayTickStr);
        } catch (NumberFormatException e) {
            //可能存在变量未替换的情况,先赋值为null,在执行时再次解析
            delayTick = null;
        }
    }

    @Override
    public String toActionStr() {
        return delayTickStr != null ? "delay " + delayTickStr : "delay";
    }

    @Override
    public void run(Player player, @NotNull Plugin plugin, @Nullable Function<String, String> argPreprocessor) {
        if (delayTick == null) {
            if (argPreprocessor != null) {
                String delayTickStr = argPreprocessor.apply(this.delayTickStr);
                delayTick = Integer.parseInt(Objects.requireNonNull(delayTickStr));
            }
        }
        Runnable actionTask = () -> {
            runNext(player, plugin, argPreprocessor);
        };
        if (player != null) {
            CrypticLibBukkit.scheduler().runOnEntityLater(player, actionTask, actionTask, delayTick);
        } else {
            CrypticLibBukkit.scheduler().syncLater(actionTask, delayTick);
        }
    }

}
