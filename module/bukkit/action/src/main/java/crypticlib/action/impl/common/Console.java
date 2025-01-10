package crypticlib.action.impl.common;

import crypticlib.action.BaseAction;
import crypticlib.chat.BukkitTextProcessor;
import crypticlib.util.StringHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public class Console extends BaseAction {

    public final String command;

    public Console(String command) {
        this.command = Objects.requireNonNull(command);
    }

    @Override
    public String toActionStr() {
        return "console " + command;
    }

    @Override
    public void run(Player player, @NotNull Plugin plugin, @Nullable Function<String, String> argPreprocessor) {
        String command = this.command;
        if (argPreprocessor != null) {
            command = argPreprocessor.apply(command);
        }
        command = BukkitTextProcessor.placeholder(player, command);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        runNext(player, plugin, argPreprocessor);
    }

}
