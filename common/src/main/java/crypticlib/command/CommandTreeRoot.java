package crypticlib.command;

import crypticlib.CrypticLib;
import crypticlib.perm.PermInfo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * CrypticLib提供的命令树根类，用于注册插件命令
 */
public class CommandTreeRoot implements ICommandNode, TabExecutor {

    private final Map<String, CommandTreeNode> nodes = new ConcurrentHashMap<>();
    private CommandTreeInfo commandTreeInfo;
    private BiFunction<CommandSender, List<String>, List<String>> tabCompleter;
    private BiFunction<CommandSender, List<String>, Boolean> executor;
    private Boolean registered = false;

    public CommandTreeRoot(CommandTreeInfo commandTreeInfo) {
        this.commandTreeInfo = commandTreeInfo;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return onTabComplete(sender, Arrays.asList(args));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return onCommand(sender, Arrays.asList(args));
    }

    public CommandTreeRoot setRootCommandInfo(CommandTreeInfo commandTreeInfo) {
        this.commandTreeInfo = commandTreeInfo;
        return this;
    }

    @Override
    public CommandTreeRoot regNode(@NotNull CommandTreeNode commandTreeNode) {
        return (CommandTreeRoot) ICommandNode.super.regNode(commandTreeNode);
    }

    @Override
    public CommandTreeRoot regNode(@NotNull String name, @NotNull BiFunction<CommandSender, List<String>, Boolean> executor) {
        return (CommandTreeRoot) ICommandNode.super.regNode(name, executor);
    }

    public CommandTreeInfo rootCommandInfo() {
        return commandTreeInfo;
    }

    @Override
    public @NotNull Map<String, CommandTreeNode> nodes() {
        return nodes;
    }

    @Override
    @Nullable
    public BiFunction<CommandSender, List<String>, Boolean> executor() {
        return executor;
    }

    @Override
    public CommandTreeRoot setExecutor(@Nullable BiFunction<CommandSender, List<String>, Boolean> executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public @NotNull BiFunction<CommandSender, List<String>, List<String>> tabCompleter() {
        return tabCompleter;
    }

    @Override
    @NotNull
    public CommandTreeRoot setTabCompleter(@NotNull BiFunction<CommandSender, List<String>, List<String>> tabCompleter) {
        this.tabCompleter = tabCompleter;
        return this;
    }
    
    public void register(@NotNull Plugin plugin) {
        if (registered)
            throw new UnsupportedOperationException("Cannot register a command repeatedly");
        registered = true;
        scanNodes();
        registerPerms();
        CrypticLib.commandManager().register(plugin, commandTreeInfo, this);
    }

    @Override
    public void registerPerms() {
        ICommandNode.super.registerPerms();
        PermInfo permission = commandTreeInfo.permission();
        if (permission != null)
            permission.register();
    }

}
