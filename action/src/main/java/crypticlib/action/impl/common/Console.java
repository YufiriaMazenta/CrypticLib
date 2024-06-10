package crypticlib.action.impl.common;

import crypticlib.action.BaseAction;
import crypticlib.chat.TextProcessor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Console extends BaseAction {

    public String command;

    public Console(String command) {
        this.command = command;
    }

    @Override
    public String toActionStr() {
        return "console " + command;
    }

    @Override
    public void run(Player player, Plugin plugin) {
        String command = TextProcessor.placeholder(player, this.command);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        runNext(player, plugin);
    }

}
