package crypticlib.action.impl;

import crypticlib.action.BaseAction;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ErrorAction extends BaseAction {
    @Override
    public void run(Player executor, Plugin plugin) {
        runNext(executor, plugin);
    }
}
