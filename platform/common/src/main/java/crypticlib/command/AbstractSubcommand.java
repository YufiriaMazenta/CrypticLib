package crypticlib.command;

import crypticlib.perm.PermInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * CrypticLib提供的子命令类
 */
public abstract class AbstractSubcommand<CommandSender> implements CommandHandler<CommandSender> {

    protected final Map<String, AbstractSubcommand<CommandSender>> subcommands = new ConcurrentHashMap<>();
    protected final CommandInfo subcommandInfo;

    public AbstractSubcommand(@NotNull CommandInfo subcommandInfo) {
        this.subcommandInfo = subcommandInfo;
    }

    public AbstractSubcommand(@NotNull String name) {
        this(name, null, new ArrayList<>());
    }

    public AbstractSubcommand(@NotNull String name, @NotNull List<String> aliases) {
        this(name, null, aliases);
    }


    public AbstractSubcommand(@NotNull String name, @Nullable PermInfo permission) {
        this(name, permission, new ArrayList<>());
    }

    public AbstractSubcommand(@NotNull String name, @Nullable PermInfo permission, @NotNull List<String> aliases) {
        this.subcommandInfo = new CommandInfo(name, permission, aliases);
    }

    @Override
    public final void onCommand(CommandSender sender, List<String> args) {
        CommandHandler.super.onCommand(sender, args);
    }

    @Override
    public final List<String> onTabComplete(CommandSender sender, List<String> args) {
        return CommandHandler.super.onTabComplete(sender, args);
    }

    /**
     * 获取此子命令的名字
     *
     * @return 子命令的名字
     */
    @NotNull
    public String name() {
        return subcommandInfo.name();
    }

    public AbstractSubcommand<CommandSender> setName(String name) {
        this.subcommandInfo.setName(name);
        return this;
    }

    @Nullable
    public PermInfo permission() {
        return subcommandInfo.permission();
    }

    public AbstractSubcommand<CommandSender> setPermission(@NotNull String permission) {
        this.subcommandInfo.setPermission(new PermInfo(permission));
        return this;
    }

    public AbstractSubcommand<CommandSender> setPermission(@Nullable PermInfo permission) {
        this.subcommandInfo.setPermission(permission);
        return this;
    }

    /**
     * 获取此子命令的别名
     *
     * @return 子命令的别名
     */
    @NotNull
    public List<String> aliases() {
        return subcommandInfo.aliases();
    }

    public AbstractSubcommand<CommandSender> setAliases(@NotNull List<String> aliases) {
        this.subcommandInfo.setAliases(aliases);
        return this;
    }

    public AbstractSubcommand<CommandSender> addAliases(@NotNull String alias) {
        this.subcommandInfo.aliases().add(alias);
        return this;
    }

    @Override
    public @NotNull Map<String, AbstractSubcommand<CommandSender>> subcommands() {
        return subcommands;
    }

    @Override
    public AbstractSubcommand<CommandSender> regSub(@NotNull AbstractSubcommand<CommandSender> subcommandHandler) {
        return (AbstractSubcommand<CommandSender>) CommandHandler.super.regSub(subcommandHandler);
    }

    public void registerPerms() {
        CommandHandler.super.registerPerms();
        PermInfo permission = permission();
        if (permission != null)
            permission.register();
    }

    @Override
    public CommandInfo commandInfo() {
        return subcommandInfo;
    }

}
