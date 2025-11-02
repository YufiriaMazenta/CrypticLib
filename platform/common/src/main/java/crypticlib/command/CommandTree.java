package crypticlib.command;

import crypticlib.CrypticLib;
import crypticlib.perm.PermInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * CrypticLib提供的命令树类,用于注册根命令
 */
public class CommandTree extends CommandNode {

    public CommandTree(@NotNull CommandInfo commandInfo) {
        super(commandInfo);
    }

    public CommandTree(@NotNull String name) {
        super(name);
    }

    public CommandTree(@NotNull String name, @NotNull List<String> aliases) {
        super(name, aliases);
    }

    public CommandTree(@NotNull String name, @Nullable PermInfo permission) {
        super(name, permission);
    }

    public CommandTree(@NotNull String name, @Nullable PermInfo permission, @NotNull List<String> aliases) {
        super(name, permission, aliases);
    }

    @Override
    public CommandTree regSub(@NotNull CommandNode commandNode) {
        return (CommandTree) super.regSub(commandNode);
    }

    public final void register() {
        scanSubCommands();
        registerPerms();
        CrypticLib.commandManager().register(this);
    }
    
}
