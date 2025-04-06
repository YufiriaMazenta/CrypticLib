package crypticlib.command;

import crypticlib.perm.PermInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CrypticLib提供的命令节点类
 */
public class CommandNode implements CommandHandler {

    protected final Map<String, CommandNode> subcommands = new ConcurrentHashMap<>();
    protected final CommandInfo commandInfo;

    public CommandNode(@NotNull CommandInfo commandInfo) {
        this.commandInfo = commandInfo;
    }

    public CommandNode(@NotNull String name) {
        this(name, null, new ArrayList<>());
    }

    public CommandNode(@NotNull String name, @NotNull List<String> aliases) {
        this(name, null, aliases);
    }


    public CommandNode(@NotNull String name, @Nullable PermInfo permission) {
        this(name, permission, new ArrayList<>());
    }

    public CommandNode(@NotNull String name, @Nullable PermInfo permission, @NotNull List<String> aliases) {
        this.commandInfo = new CommandInfo(name, permission, aliases);
    }

    @Override
    public final void onCommand(CommandInvoker invoker, List<String> args) {
        CommandHandler.super.onCommand(invoker, args);
    }

    @Override
    public final List<String> onTabComplete(CommandInvoker invoker, List<String> args) {
        return CommandHandler.super.onTabComplete(invoker, args);
    }

    @Override
    public void sendDescriptions(CommandInvoker invoker) {
        List<String> descriptions = toDescriptions(invoker);
        for (String description : descriptions) {
            invoker.sendMsg(description);
        }
    }

    @Override
    public final @NotNull Map<String, CommandNode> subcommands() {
        return subcommands;
    }

    @Override
    public CommandNode regSub(@NotNull CommandNode commandNodeHandler) {
        return (CommandNode) CommandHandler.super.regSub(commandNodeHandler);
    }

    @Override
    public final @NotNull CommandInfo commandInfo() {
        return commandInfo;
    }

}
