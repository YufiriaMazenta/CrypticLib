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
public class SubcommandHandler implements ICommandHandler {

    protected final Map<String, SubcommandHandler> subcommands = new ConcurrentHashMap<>();
    protected final SubcommandInfo subcommandInfo;

    public SubcommandHandler(@NotNull SubcommandInfo subcommandInfo) {
        this.subcommandInfo = subcommandInfo;
    }

    public SubcommandHandler(@NotNull String name) {
        this(name, null, new ArrayList<>());
    }

    public SubcommandHandler(@NotNull String name, @NotNull List<String> aliases) {
        this(name, null, aliases);
    }


    public SubcommandHandler(@NotNull String name, @Nullable PermInfo permission) {
        this(name, permission, new ArrayList<>());
    }

    public SubcommandHandler(@NotNull String name, @Nullable PermInfo permission, @NotNull List<String> aliases) {
        this.subcommandInfo = new SubcommandInfo(name, permission, aliases);
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

    public SubcommandHandler setName(String name) {
        this.subcommandInfo.setName(name);
        return this;
    }

    @Nullable
    public PermInfo permission() {
        return subcommandInfo.permission();
    }

    public SubcommandHandler setPermission(PermInfo permission) {
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

    public SubcommandHandler setAliases(@NotNull List<String> aliases) {
        this.subcommandInfo.setAliases(aliases);
        return this;
    }

    public SubcommandHandler addAliases(@NotNull String alias) {
        this.subcommandInfo.aliases().add(alias);
        return this;
    }

    @Override
    public @NotNull Map<String, SubcommandHandler> subcommands() {
        return subcommands;
    }

    @Override
    public SubcommandHandler regSub(@NotNull SubcommandHandler subcommandHandler) {
        return (SubcommandHandler) ICommandHandler.super.regSub(subcommandHandler);
    }

    public void registerPerms() {
        ICommandHandler.super.registerPerms();
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
