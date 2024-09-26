package crypticlib.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.permission.Tristate;
import crypticlib.chat.VelocityMsgSender;
import crypticlib.perm.PermDef;
import crypticlib.perm.PermInfo;
import crypticlib.VelocityPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VelocityCommand implements SimpleCommand, CommandHandler<CommandSource>  {

    protected final Map<String, AbstractSubcommand<CommandSource>> subcommands = new ConcurrentHashMap<>();
    protected CommandInfo commandInfo;
    protected Boolean registered = false;

    public VelocityCommand(CommandInfo commandInfo) {
        this.commandInfo = commandInfo;
    }

    @Override
    public final void execute(Invocation invocation) {
        List<String> arguments = Arrays.asList(invocation.arguments());
        onCommand(invocation.source(), arguments);
    }

    @Override
    public final void onCommand(CommandSource sender, List<String> args) {
        CommandHandler.super.onCommand(sender, args);
    }

    @Override
    public final List<String> suggest(Invocation invocation) {
        List<String> arguments = Arrays.asList(invocation.arguments());
        return onTabComplete(invocation.source(), arguments);
    }

    @Override
    public final List<String> onTabComplete(CommandSource sender, List<String> args) {
        return CommandHandler.super.onTabComplete(sender, args);
    }

    @Override
    public void sendDescriptions(CommandSource commandSource) {
        List<String> descriptions = toDescriptions();
        for (String description : descriptions) {
            VelocityMsgSender.INSTANCE.sendMsg(commandSource, description);
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        PermInfo permInfo = commandInfo.permission();
        if (permInfo == null)
            return true;
        if (permInfo.hasPermission(invocation.source())) {
            return true;
        }
        Tristate tristate = invocation.source().getPermissionValue(permInfo.permission());
        return permInfo.permDef().equals(PermDef.TRUE) && tristate != Tristate.FALSE;
    }

    @Override
    public void registerPerms() {
        CommandHandler.super.registerPerms();
        PermInfo permission = commandInfo.permission();
        if (permission != null)
            permission.register();
    }

    public void register(@NotNull VelocityPlugin plugin) {
        if (registered)
            throw new UnsupportedOperationException("Cannot register a command repeatedly");
        registered = true;
        scanSubCommands();
        registerPerms();
        VelocityCommandManager.INSTANCE.register(plugin, commandInfo, this);
    }

    @Override
    public VelocityCommand regSub(@NotNull AbstractSubcommand<CommandSource> subcommandHandler) {
        return (VelocityCommand) CommandHandler.super.regSub(subcommandHandler);
    }

    @Override
    public CommandInfo commandInfo() {
        return commandInfo;
    }

    @Override
    public @NotNull Map<String, AbstractSubcommand<CommandSource>> subcommands() {
        return subcommands;
    }
}
