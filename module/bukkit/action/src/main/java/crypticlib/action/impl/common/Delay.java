package crypticlib.action.impl.common;

import crypticlib.CrypticLibBukkit;
import crypticlib.action.BaseAction;
import crypticlib.util.StringHelper;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class Delay extends BaseAction {

    private Integer delayTick;
    private String delayTickStr;

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
    public void run(Player player, @NotNull Plugin plugin, Map<String, String> args) {
        if (delayTick == null) {
            String delayTickStr = StringHelper.replaceStrings(this.delayTickStr, args);
            delayTick = Integer.parseInt(Objects.requireNonNull(delayTickStr));
        }
        CrypticLibBukkit.platform().scheduler().syncLater(() -> {
            runNext(player, plugin, args);
        }, delayTick);
    }

}
