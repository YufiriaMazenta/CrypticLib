package crypticlib.action.impl.common;

import crypticlib.CrypticLibBukkit;
import crypticlib.action.BaseAction;
import crypticlib.chat.BukkitTextProcessor;
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
        if (CrypticLibBukkit.isFolia()) {
            String finalCommand = command;
            CrypticLibBukkit.scheduler().sync(() -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            });
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }

        runNext(player, plugin, argPreprocessor);
    }

}
