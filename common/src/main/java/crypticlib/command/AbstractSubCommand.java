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
public abstract class AbstractSubCommand<C> implements CommandHandler<C> {

    protected final Map<String, AbstractSubCommand<C>> subcommands = new ConcurrentHashMap<>();
    protected final SubcommandInfo subcommandInfo;

    public AbstractSubCommand(@NotNull SubcommandInfo subcommandInfo) {
        this.subcommandInfo = subcommandInfo;
    }

    public AbstractSubCommand(@NotNull String name) {
        this(name, null, new ArrayList<>());
    }

    public AbstractSubCommand(@NotNull String name, @NotNull List<String> aliases) {
        this(name, null, aliases);
    }


    public AbstractSubCommand(@NotNull String name, @Nullable PermInfo permission) {
        this(name, permission, new ArrayList<>());
    }

    public AbstractSubCommand(@NotNull String name, @Nullable PermInfo permission, @NotNull List<String> aliases) {
        this.subcommandInfo = new SubcommandInfo(name, permission, aliases);
    }

    @Override
    public final boolean onCommand(C sender, List<String> args) {
        return CommandHandler.super.onCommand(sender, args);
    }

    @Override
    public final List<String> onTabComplete(C sender, List<String> args) {
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

    public AbstractSubCommand<C> setName(String name) {
        this.subcommandInfo.setName(name);
        return this;
    }

    @Nullable
    public PermInfo permission() {
        return subcommandInfo.permission();
    }

    public AbstractSubCommand<C> setPermission(@NotNull String permission) {
        this.subcommandInfo.setPermission(new PermInfo(permission));
        return this;
    }

    public AbstractSubCommand<C> setPermission(@Nullable PermInfo permission) {
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

    public AbstractSubCommand<C> setAliases(@NotNull List<String> aliases) {
        this.subcommandInfo.setAliases(aliases);
        return this;
    }

    public AbstractSubCommand<C> addAliases(@NotNull String alias) {
        this.subcommandInfo.aliases().add(alias);
        return this;
    }

    @Override
    public @NotNull Map<String, AbstractSubCommand<C>> subcommands() {
        return subcommands;
    }

    @Override
    public AbstractSubCommand<C> regSub(@NotNull AbstractSubCommand<C> subcommandHandler) {
        return (AbstractSubCommand<C>) CommandHandler.super.regSub(subcommandHandler);
    }

    public void registerPerms() {
        CommandHandler.super.registerPerms();
        PermInfo permission = permission();
        if (permission != null)
            permission.register();
    }

    public static class SubcommandInfo {

        private String name;
        private PermInfo permission;
        private final List<String> aliases = new CopyOnWriteArrayList<>();

        public SubcommandInfo(String name) {
            this.name = name;
        }

        public SubcommandInfo(String name, PermInfo permission) {
            this.name = name;
            this.permission = permission;
        }

        public SubcommandInfo(String name, PermInfo permission, List<String> aliases) {
            this.name = name;
            this.permission = permission;
            this.aliases.addAll(aliases);
        }

        @NotNull
        public String name() {
            return name;
        }

        public SubcommandInfo setName(String name) {
            this.name = name;
            return this;
        }

        @Nullable
        public PermInfo permission() {
            return permission;
        }

        public SubcommandInfo setPermission(PermInfo permission) {
            this.permission = permission;
            return this;
        }

        @NotNull
        public List<String> aliases() {
            return aliases;
        }

        public SubcommandInfo setAliases(List<String> aliases) {
            this.aliases.clear();
            this.aliases.addAll(aliases);
            return this;
        }
    }
}
