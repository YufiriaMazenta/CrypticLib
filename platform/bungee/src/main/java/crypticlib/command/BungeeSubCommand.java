package crypticlib.command;

import crypticlib.perm.PermInfo;
import net.md_5.bungee.api.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class BungeeSubCommand extends AbstractSubCommand<CommandSender> {

    public BungeeSubCommand(@NotNull SubcommandInfo subcommandInfo) {
        super(subcommandInfo);
    }

    public BungeeSubCommand(@NotNull String name) {
        super(name);
    }

    public BungeeSubCommand(@NotNull String name, @NotNull List<String> aliases) {
        super(name, aliases);
    }

    public BungeeSubCommand(@NotNull String name, @Nullable PermInfo permission) {
        super(name, permission);
    }

    public BungeeSubCommand(@NotNull String name, @Nullable PermInfo permission, @NotNull List<String> aliases) {
        super(name, permission, aliases);
    }

    @Override
    public BungeeSubCommand setName(String name) {
        return (BungeeSubCommand) super.setName(name);
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
    public BungeeSubCommand setPermission(@NotNull String permission) {
        return (BungeeSubCommand) super.setPermission(permission);
    }

    @Override
    public BungeeSubCommand setPermission(@Nullable PermInfo permission) {
        return (BungeeSubCommand) super.setPermission(permission);
    }

    @Override
    public @NotNull List<String> aliases() {
        return super.aliases();
    }

    @Override
    public BungeeSubCommand setAliases(@NotNull List<String> aliases) {
        return (BungeeSubCommand) super.setAliases(aliases);
    }

    @Override
    public @NotNull Map<String, AbstractSubCommand<CommandSender>> subcommands() {
        return super.subcommands();
    }

    @Override
    public BungeeSubCommand addAliases(@NotNull String alias) {
        return (BungeeSubCommand) super.addAliases(alias);
    }

    @Override
    public BungeeSubCommand regSub(@NotNull AbstractSubCommand<CommandSender> subcommandHandler) {
        return (BungeeSubCommand) super.regSub(subcommandHandler);
    }

    @Override
    public void registerPerms() {
        super.registerPerms();
    }
}
