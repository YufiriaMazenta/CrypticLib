package crypticlib.command;

import crypticlib.perm.PermInfo;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;

/**
 * CrypticLib提供的命令树节点类，即子命令
 */
public class CommandTreeNode implements ICommandNode {

    private final Map<String, CommandTreeNode> nodes = new ConcurrentHashMap<>();
    private final NodeInfo nodeInfo;
    private BiFunction<CommandSender, List<String>, List<String>> tabCompleter;
    private BiFunction<CommandSender, List<String>, Boolean> executor;

    public CommandTreeNode(@NotNull CommandTreeNode.NodeInfo nodeInfo) {
        this(nodeInfo, null);
    }

    public CommandTreeNode(@NotNull CommandTreeNode.NodeInfo nodeInfo, @Nullable BiFunction<CommandSender, List<String>, Boolean> executor) {
        this.nodeInfo = nodeInfo;
        this.executor = executor;
    }

    public CommandTreeNode(@NotNull String name) {
        this(name, null, new ArrayList<>());
    }

    public CommandTreeNode(@NotNull String name, @Nullable BiFunction<CommandSender, List<String>, Boolean> executor) {
        this(name, null, new ArrayList<>(), executor);
    }

    public CommandTreeNode(@NotNull String name, @NotNull List<String> aliases) {
        this(name, null, aliases, null);
    }

    public CommandTreeNode(@NotNull String name, @NotNull List<String> aliases, @Nullable BiFunction<CommandSender, List<String>, Boolean> executor) {
        this(name, null, aliases, executor);
    }

    public CommandTreeNode(@NotNull String name, @Nullable PermInfo permission) {
        this(name, permission, new ArrayList<>(), null);
    }

    public CommandTreeNode(@NotNull String name, @Nullable PermInfo permission, @Nullable BiFunction<CommandSender, List<String>, Boolean> executor) {
        this(name, permission, new ArrayList<>(), executor);
    }

    public CommandTreeNode(@NotNull String name, @Nullable PermInfo permission, @NotNull List<String> aliases) {
        this(name, permission, aliases, null);
    }

    public CommandTreeNode(@NotNull String name, @Nullable PermInfo permission, @NotNull List<String> aliases, @Nullable BiFunction<CommandSender, List<String>, Boolean> executor) {
        this.nodeInfo = new NodeInfo(name, permission, aliases);
        this.executor = executor;
    }

    @Override
    @Nullable
    public BiFunction<CommandSender, List<String>, Boolean> executor() {
        return executor;
    }

    @Override
    public CommandTreeNode setExecutor(@Nullable BiFunction<CommandSender, List<String>, Boolean> executor) {
        this.executor = executor;
        return this;
    }

    @Override
    @NotNull
    public CommandTreeNode setTabCompleter(@NotNull BiFunction<CommandSender, List<String>, List<String>> tabCompleter) {
        this.tabCompleter = tabCompleter;
        return this;
    }

    @Override
    public @NotNull BiFunction<CommandSender, List<String>, List<String>> tabCompleter() {
        return tabCompleter;
    }

    /**
     * 获取此子命令的名字
     *
     * @return 子命令的名字
     */
    @NotNull
    public String name() {
        return nodeInfo.name();
    }

    public CommandTreeNode setName(String name) {
        this.nodeInfo.setName(name);
        return this;
    }

    @Nullable
    public PermInfo permission() {
        return nodeInfo.permission();
    }

    public CommandTreeNode setPermission(PermInfo permission) {
        this.nodeInfo.setPermission(permission);
        return this;
    }

    /**
     * 获取此子命令的别名
     *
     * @return 子命令的别名
     */
    @NotNull
    public List<String> aliases() {
        return nodeInfo.aliases();
    }

    public CommandTreeNode setAliases(@NotNull List<String> aliases) {
        this.nodeInfo.setAliases(aliases);
        return this;
    }

    public CommandTreeNode addAliases(@NotNull String alias) {
        this.nodeInfo.aliases().add(alias);
        return this;
    }

    @Override
    public @NotNull Map<String, CommandTreeNode> nodes() {
        return nodes;
    }

    @Override
    public CommandTreeNode regNode(@NotNull CommandTreeNode commandTreeNode) {
        return (CommandTreeNode) ICommandNode.super.regNode(commandTreeNode);
    }

    @Override
    public CommandTreeNode regNode(@NotNull String name, @NotNull BiFunction<CommandSender, List<String>, Boolean> executor) {
        return (CommandTreeNode) ICommandNode.super.regNode(name, executor);
    }

    public void registerPerms() {
        ICommandNode.super.registerPerms();
        PermInfo permission = permission();
        if (permission != null)
            permission.register();
    }

    public static class NodeInfo {

        private String name;
        private PermInfo permission;
        private final List<String> aliases = new CopyOnWriteArrayList<>();

        public NodeInfo(String name) {
            this.name = name;
        }

        public NodeInfo(String name, PermInfo permission) {
            this.name = name;
            this.permission = permission;
        }

        public NodeInfo(String name, PermInfo permission, List<String> aliases) {
            this.name = name;
            this.permission = permission;
            this.aliases.addAll(aliases);
        }

        @NotNull
        public String name() {
            return name;
        }

        public NodeInfo setName(String name) {
            this.name = name;
            return this;
        }

        @Nullable
        public PermInfo permission() {
            return permission;
        }

        public NodeInfo setPermission(PermInfo permission) {
            this.permission = permission;
            return this;
        }

        @NotNull
        public List<String> aliases() {
            return aliases;
        }

        public NodeInfo setAliases(List<String> aliases) {
            this.aliases.clear();
            this.aliases.addAll(aliases);
            return this;
        }
    }
}
