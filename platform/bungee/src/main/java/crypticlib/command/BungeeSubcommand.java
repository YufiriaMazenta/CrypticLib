package crypticlib.command;

import crypticlib.chat.BungeeMsgSender;
import crypticlib.perm.PermInfo;
import net.md_5.bungee.api.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class BungeeSubcommand extends AbstractSubcommand<CommandSender> {

    public BungeeSubcommand(@NotNull CommandInfo subcommandInfo) {
        super(subcommandInfo);
    }

    public BungeeSubcommand(@NotNull String name) {
        super(name);
    }

    public BungeeSubcommand(@NotNull String name, @NotNull List<String> aliases) {
        super(name, aliases);
    }

    public BungeeSubcommand(@NotNull String name, @Nullable PermInfo permission) {
        super(name, permission);
    }

    public BungeeSubcommand(@NotNull String name, @Nullable PermInfo permission, @NotNull List<String> aliases) {
        super(name, permission, aliases);
    }

    @Override
    public BungeeSubcommand setName(String name) {
        return (BungeeSubcommand) super.setName(name);
    }

    @Override
    public @NotNull String name() {
        return super.name();
    }

    @Override
    public @Nullable PermInfo permission() {
        return super.permission();
    }

    @Override
    public BungeeSubcommand setPermission(@NotNull String permission) {
        return (BungeeSubcommand) super.setPermission(permission);
    }

    @Override
    public BungeeSubcommand setPermission(@Nullable PermInfo permission) {
        return (BungeeSubcommand) super.setPermission(permission);
    }

    @Override
    public @NotNull List<String> aliases() {
        return super.aliases();
    }

    @Override
    public BungeeSubcommand setAliases(@NotNull List<String> aliases) {
        return (BungeeSubcommand) super.setAliases(aliases);
    }

    @Override
    public @NotNull Map<String, AbstractSubcommand<CommandSender>> subcommands() {
        return super.subcommands();
    }

    @Override
    public BungeeSubcommand addAliases(@NotNull String alias) {
        return (BungeeSubcommand) super.addAliases(alias);
    }

    @Override
    public BungeeSubcommand regSub(@NotNull AbstractSubcommand<CommandSender> subcommandHandler) {
        return (BungeeSubcommand) super.regSub(subcommandHandler);
    }

    @Override
    public void registerPerms() {
        super.registerPerms();
    }

    @Override
    public void sendDescriptions(CommandSender commandSender) {
        List<String> descriptions = toDescriptions();
        for (String description : descriptions) {
            BungeeMsgSender.INSTANCE.sendMsg(commandSender, description);
        }
    }

}
