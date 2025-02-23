package crypticlib.command;

import crypticlib.chat.BukkitMsgSender;
import crypticlib.perm.PermInfo;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class BukkitSubcommand extends AbstractSubcommand<CommandSender> {

    public BukkitSubcommand(@NotNull CommandInfo subcommandInfo) {
        super(subcommandInfo);
    }

    public BukkitSubcommand(@NotNull String name) {
        super(name);
    }

    public BukkitSubcommand(@NotNull String name, @NotNull List<String> aliases) {
        super(name, aliases);
    }

    public BukkitSubcommand(@NotNull String name, @Nullable PermInfo permission) {
        super(name, permission);
    }

    public BukkitSubcommand(@NotNull String name, @Nullable PermInfo permission, @NotNull List<String> aliases) {
        super(name, permission, aliases);
    }

    @Override
    public final BukkitSubcommand setName(String name) {
        return (BukkitSubcommand) super.setName(name);
    }

    @Override
    public final BukkitSubcommand setPermission(@NotNull String permission) {
        return (BukkitSubcommand) super.setPermission(permission);
    }

    @Override
    public final BukkitSubcommand setPermission(@Nullable PermInfo permission) {
        return (BukkitSubcommand) super.setPermission(permission);
    }

    @Override
    public final BukkitSubcommand setAliases(@NotNull List<String> aliases) {
        return (BukkitSubcommand) super.setAliases(aliases);
    }


    @Override
    public final BukkitSubcommand addAliases(@NotNull String alias) {
        return (BukkitSubcommand) super.addAliases(alias);
    }

    @Override
    public final BukkitSubcommand regSub(@NotNull AbstractSubcommand<CommandSender> subcommandHandler) {
        return (BukkitSubcommand) super.regSub(subcommandHandler);
    }

    @Override
    public void sendDescriptions(CommandSender commandSender) {
        List<String> descriptions = toDescriptions(commandSender);
        for (String description : descriptions) {
            BukkitMsgSender.INSTANCE.sendMsg(commandSender, description);
        }
    }

}
