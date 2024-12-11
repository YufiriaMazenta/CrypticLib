package crypticlib.command;

import crypticlib.chat.BungeeMsgSender;
import crypticlib.perm.PermInfo;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CrypticLib提供的插件命令类，用于注册插件命令
 */
public class BungeeCommand extends Command implements CommandHandler<CommandSender>, TabExecutor {

    protected final Map<String, AbstractSubcommand<CommandSender>> subcommands = new ConcurrentHashMap<>();
    protected CommandInfo commandInfo;
    protected Boolean registered = false;

    public BungeeCommand(CommandInfo commandInfo) {
        super(commandInfo.name(), commandInfo.permission() != null ? commandInfo.permission().permission() : null, commandInfo.aliases().toArray(new String[]{}));
        this.commandInfo = commandInfo;
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
    public BungeeCommand regSub(@NotNull AbstractSubcommand<CommandSender> subcommandHandler) {
        return (BungeeCommand) CommandHandler.super.regSub(subcommandHandler);
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
        BungeeCommandManager.INSTANCE.register(plugin, commandInfo, this);
    }

    @Override
    public void registerPerms() {
        CommandHandler.super.registerPerms();
        PermInfo permission = commandInfo.permission();
        if (permission != null)
            permission.register();
    }

    @Override
    public void sendDescriptions(CommandSender commandSender) {
        List<String> descriptions = toDescriptions(commandSender);
        for (String description : descriptions) {
            BungeeMsgSender.INSTANCE.sendMsg(commandSender, description);
        }
    }

    @Override
    public CommandInfo commandInfo() {
        return commandInfo;
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        onCommand(sender, Arrays.asList(args));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return onTabComplete(sender, Arrays.asList(args));
    }

}
