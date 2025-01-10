package crypticlib.action.impl.common;

import crypticlib.action.BaseAction;
import crypticlib.chat.BukkitTextProcessor;
import crypticlib.util.StringHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class Console extends BaseAction {

    public String command;

    public Console(String command) {
        this.command = Objects.requireNonNull(command);
    }

    @Override
    public String toActionStr() {
        return "console " + command;
    }

    @Override
    public void run(Player player, @NotNull Plugin plugin, Map<String, String> args) {
        String command = BukkitTextProcessor.placeholder(player, StringHelper.replaceStrings(this.command, args));
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        runNext(player, plugin, args);
    }

}
