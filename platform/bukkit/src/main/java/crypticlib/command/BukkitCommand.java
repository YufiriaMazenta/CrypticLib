package crypticlib.command;

import crypticlib.perm.PermInfo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CrypticLib提供的插件命令类，用于注册插件命令
 */
public class BukkitCommand implements CommandHandler<CommandSender>, TabExecutor {

    protected final Map<String, AbstractSubcommand<CommandSender>> subcommands = new ConcurrentHashMap<>();
    protected CommandInfo commandInfo;
    protected Boolean registered = false;

    public BukkitCommand(CommandInfo commandInfo) {
        this.commandInfo = commandInfo;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return onTabComplete(sender, Arrays.asList(args));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        onCommand(sender, Arrays.asList(args));
        return true;
    }

    public BukkitCommand setRootCommandInfo(CommandInfo commandInfo) {
        this.commandInfo = commandInfo;
        return this;
    }

    @Override
    public final void onCommand(CommandSender sender, List<String> args) {
        CommandHandler.super.onCommand(sender, args);
    }

    @Override
    public final List<String> onTabComplete(CommandSender sender, List<String> args) {
        return CommandHandler.super.onTabComplete(sender, args);
    }

    @Override
    public BukkitCommand regSub(@NotNull AbstractSubcommand<CommandSender> subcommandHandler) {
        return (BukkitCommand) CommandHandler.super.regSub(subcommandHandler);
    }

    public CommandInfo rootCommandInfo() {
        return commandInfo;
    }

    @Override
    public @NotNull Map<String, AbstractSubcommand<CommandSender>> subcommands() {
        return subcommands;
    }
    
    public void register(@NotNull Plugin plugin) {
        if (registered)
            throw new UnsupportedOperationException("Cannot register a command repeatedly");
        registered = true;
        scanSubCommands();
        registerPerms();
        BukkitCommandManager.INSTANCE.register(plugin, commandInfo, this);
    }

    @Override
    public void registerPerms() {
        CommandHandler.super.registerPerms();
        PermInfo permission = commandInfo.permission();
        if (permission != null)
            permission.register();
    }

}
