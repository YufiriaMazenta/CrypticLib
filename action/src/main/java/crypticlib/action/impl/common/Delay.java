package crypticlib.action.impl.common;

import crypticlib.CrypticLib;
import crypticlib.action.BaseAction;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Delay extends BaseAction {

    private int delayTick;

    public Delay(String delayTickStr) {
        if (delayTickStr == null)
            delayTick = 0;
        else
            delayTick = Integer.parseInt(delayTickStr);
    }

    public int delayTick() {
        return delayTick;
    }

    public Delay setDelayTick(int delayTick) {
        this.delayTick = delayTick;
        return this;
    }

    @Override
    public String toActionStr() {
        return "delay " + delayTick;
    }

    @Override
    public void run(Player player, Plugin plugin) {
        CrypticLib.platform().scheduler().runTaskLater(plugin, () -> {
            runNext(player, plugin);
        }, delayTick);
    }

}
