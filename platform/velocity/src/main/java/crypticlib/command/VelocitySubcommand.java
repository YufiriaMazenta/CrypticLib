package crypticlib.command;

import com.velocitypowered.api.command.CommandSource;
import crypticlib.chat.VelocityMsgSender;
import crypticlib.perm.PermInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VelocitySubcommand extends AbstractSubcommand<CommandSource> {

    public VelocitySubcommand(@NotNull CommandInfo subcommandInfo) {
        super(subcommandInfo);
    }

    public VelocitySubcommand(@NotNull String name) {
        super(name);
    }

    public VelocitySubcommand(@NotNull String name, @NotNull List<String> aliases) {
        super(name, aliases);
    }

    public VelocitySubcommand(@NotNull String name, @Nullable PermInfo permission) {
        super(name, permission);
    }

    public VelocitySubcommand(@NotNull String name, @Nullable PermInfo permission, @NotNull List<String> aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void sendDescriptions(CommandSource commandSource) {
        List<String> descriptions = toDescriptions(commandSource);
        for (String description : descriptions) {
            VelocityMsgSender.INSTANCE.sendMsg(commandSource, description);
        }
    }

    @Override
    public final VelocitySubcommand regSub(@NotNull AbstractSubcommand<CommandSource> subcommandHandler) {
        return (VelocitySubcommand) super.regSub(subcommandHandler);
    }

    @Override
    public final VelocitySubcommand setName(String name) {
        return (VelocitySubcommand) super.setName(name);
    }

    @Override
    public final VelocitySubcommand setPermission(@NotNull String permission) {
        return (VelocitySubcommand) super.setPermission(permission);
    }

    @Override
    public final VelocitySubcommand setPermission(@Nullable PermInfo permission) {
        return (VelocitySubcommand) super.setPermission(permission);
    }

    @Override
    public final VelocitySubcommand setAliases(@NotNull List<String> aliases) {
        return (VelocitySubcommand) super.setAliases(aliases);
    }
}
