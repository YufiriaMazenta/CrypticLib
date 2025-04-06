package crypticlib.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import crypticlib.perm.PermDef;
import crypticlib.perm.PermInfo;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class VelocityCommand implements SimpleCommand {

    private final CommandTree commandTree;

    public VelocityCommand(CommandTree commandTree) {
        this.commandTree = commandTree;
    }

    @Override
    public void execute(Invocation invocation) {
        List<String> arguments = Arrays.asList(invocation.arguments());
        commandTree.onCommand(commandSource2Invoker(invocation.source()), arguments);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        List<String> arguments = Arrays.asList(invocation.arguments());
        return commandTree.onTabComplete(commandSource2Invoker(invocation.source()), arguments);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        future.complete(suggest(invocation));
        return future;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        CommandSource commandSource = invocation.source();
        if (commandTree.hasPermission(commandSource2Invoker(commandSource)))
            return true;
        PermInfo permInfo = commandTree.commandInfo().permission();
        if (permInfo == null)
            return true;
        //检查权限默认值,如果权限默认为true且玩家对于该权限的状态不为false,返回true
        Tristate tristate = commandSource.getPermissionValue(permInfo.permission());
        return permInfo.permDef().equals(PermDef.TRUE) && tristate != Tristate.FALSE;
    }

    private CommandInvoker commandSource2Invoker(CommandSource source) {
        if (source instanceof Player) {
            return new VelocityPlayerCommandInvoker((Player) source);
        } else {
            return new VelocityCommandInvoker(source);
        }
    }

}
