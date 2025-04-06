package crypticlib.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Arrays;

public final class BungeeCommand extends Command implements TabExecutor {

    private final CommandTree commandTree;

    public BungeeCommand(CommandTree commandTree) {
        super(
            commandTree.commandInfo.name(),
            commandTree.commandInfo.permission() != null ? commandTree.commandInfo.permission().permission() : null, commandTree.commandInfo.aliases().toArray(new String[]{})
        );
        this.commandTree = commandTree;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        commandTree.onCommand(commandSender2Invoker(sender), Arrays.asList(args));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return commandTree.onTabComplete(commandSender2Invoker(sender), Arrays.asList(args));
    }

    private CommandInvoker commandSender2Invoker(CommandSender sender) {
        if (sender instanceof ProxiedPlayer) {
            return new BungeePlayerCommandInvoker((ProxiedPlayer) sender);
        } else {
            return new BungeeCommandInvoker(sender);
        }
    }

}
