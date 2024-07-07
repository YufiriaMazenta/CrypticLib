package crypticlib.command;

import crypticlib.perm.PermInfo;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BukkitSubCommand extends AbstractSubCommand<CommandSender> {

    public BukkitSubCommand(@NotNull SubcommandInfo subcommandInfo) {
        super(subcommandInfo);
    }

    public BukkitSubCommand(@NotNull String name) {
        super(name);
    }

    public BukkitSubCommand(@NotNull String name, @NotNull List<String> aliases) {
        super(name, aliases);
    }

    public BukkitSubCommand(@NotNull String name, @Nullable PermInfo permission) {
        super(name, permission);
    }

    public BukkitSubCommand(@NotNull String name, @Nullable PermInfo permission, @NotNull List<String> aliases) {
        super(name, permission, aliases);
    }

    @Override
    public BukkitSubCommand setName(String name) {
        return (BukkitSubCommand) super.setName(name);
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
    public BukkitSubCommand setPermission(@NotNull String permission) {
        return (BukkitSubCommand) super.setPermission(permission);
    }

    @Override
    public BukkitSubCommand setPermission(@Nullable PermInfo permission) {
        return (BukkitSubCommand) super.setPermission(permission);
    }

    @Override
    public @NotNull List<String> aliases() {
        return super.aliases();
    }

    @Override
    public BukkitSubCommand setAliases(@NotNull List<String> aliases) {
        return (BukkitSubCommand) super.setAliases(aliases);
    }

    @Override
    public @NotNull Map<String, AbstractSubCommand<CommandSender>> subcommands() {
        return super.subcommands();
    }

    @Override
    public BukkitSubCommand addAliases(@NotNull String alias) {
        return (BukkitSubCommand) super.addAliases(alias);
    }

    @Override
    public BukkitSubCommand regSub(@NotNull AbstractSubCommand<CommandSender> subcommandHandler) {
        return (BukkitSubCommand) super.regSub(subcommandHandler);
    }

    @Override
    public void registerPerms() {
        super.registerPerms();
    }
}
