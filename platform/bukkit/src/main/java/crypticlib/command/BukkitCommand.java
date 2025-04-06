package crypticlib.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class BukkitCommand implements TabExecutor {

    private final CommandTree commandTree;

    public BukkitCommand(@NotNull CommandTree commandTree) {
        Objects.requireNonNull(commandTree);
        this.commandTree = commandTree;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return commandTree.onTabComplete(commandSender2Invoker(sender), Arrays.asList(args));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        commandTree.onCommand(commandSender2Invoker(sender), Arrays.asList(args));
        return true;
    }

    private CommandInvoker commandSender2Invoker(CommandSender sender) {
        if (sender instanceof Player) {
            return new BukkitPlayerCommandInvoker((Player) sender);
        } else {
            return new BukkitCommandInvoker(sender);
        }
    }

}
