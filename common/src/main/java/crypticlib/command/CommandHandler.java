package crypticlib.command;

import com.google.common.collect.Maps;
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
public class CommandHandler implements ICommandNode, TabExecutor {

    private final Map<String, SubcommandHandler> subcommands = new ConcurrentHashMap<>();
    private CommandInfo commandInfo;
    private BiFunction<CommandSender, List<String>, List<String>> tabCompleter;
    private BiFunction<CommandSender, List<String>, Boolean> executor;
    private Boolean registered = false;

    public CommandHandler(CommandInfo commandInfo) {
        this.commandInfo = commandInfo;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return onTabComplete(sender, Arrays.asList(args));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return onCommand(sender, Arrays.asList(args));
    }

    public CommandHandler setRootCommandInfo(CommandInfo commandInfo) {
        this.commandInfo = commandInfo;
        return this;
    }

    @Override
    public CommandHandler regSub(@NotNull SubcommandHandler subcommandHandler) {
        return (CommandHandler) ICommandNode.super.regSub(subcommandHandler);
    }

    @Override
    public CommandHandler regSub(@NotNull String name, @NotNull BiFunction<CommandSender, List<String>, Boolean> executor) {
        return (CommandHandler) ICommandNode.super.regSub(name, executor);
    }

    public CommandInfo rootCommandInfo() {
        return commandInfo;
    }

    @Override
    public @NotNull Map<String, SubcommandHandler> subcommands() {
        return subcommands;
    }

    @Override
    @Nullable
    public BiFunction<CommandSender, List<String>, Boolean> executor() {
        return executor;
    }

    @Override
    public CommandHandler setExecutor(@Nullable BiFunction<CommandSender, List<String>, Boolean> executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public @NotNull BiFunction<CommandSender, List<String>, List<String>> tabCompleter() {
        return tabCompleter;
    }

    @Override
    @NotNull
    public CommandHandler setTabCompleter(@NotNull BiFunction<CommandSender, List<String>, List<String>> tabCompleter) {
        this.tabCompleter = tabCompleter;
        return this;
    }
    
    public void register(@NotNull Plugin plugin) {
        if (registered)
            throw new UnsupportedOperationException("Cannot register a command repeatedly");
        registered = true;
        scanNodes();
        registerPerms();
        CrypticLib.commandManager().register(plugin, commandInfo, this);
    }

    @Override
    public void registerPerms() {
        ICommandNode.super.registerPerms();
        PermInfo permission = commandInfo.permission();
        if (permission != null)
            permission.register();
    }

}
