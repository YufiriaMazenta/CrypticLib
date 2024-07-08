package crypticlib.action.impl.common;

import crypticlib.action.BaseAction;
import crypticlib.chat.BukkitTextProcessor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Command extends BaseAction {

    public String command;

    public Command(String command) {
        this.command = command;
    }

    @Override
    public String toActionStr() {
        return "command " + command;
    }

    @Override
    public void run(Player player, Plugin plugin) {
        String command = BukkitTextProcessor.placeholder(player, this.command);
        Bukkit.dispatchCommand(player, command);
        runNext(player, plugin);
    }

}
