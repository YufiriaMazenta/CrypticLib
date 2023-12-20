package crypticlib.command.impl;

import crypticlib.command.ISubcmdExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * CrypticLib提供的子命令接口
 */
public class SubcmdExecutor implements ISubcmdExecutor {

    private final String name;
    private final Map<String, ISubcmdExecutor> subcommands;
    private String permission;
    private List<String> aliases;
    private Supplier<List<String>> tabArgsSupplier;
    private BiFunction<CommandSender, List<String>, Boolean> executor;

    public SubcmdExecutor(@NotNull String name) {
        this(name, null, new ArrayList<>());
    }

    public SubcmdExecutor(@NotNull String name, @Nullable BiFunction<CommandSender, List<String>, Boolean> executor) {
        this(name, null, new ArrayList<>(), executor);
    }

    public SubcmdExecutor(@NotNull String name, @NotNull List<String> aliases) {
        this(name, null, aliases, null);
    }

    public SubcmdExecutor(@NotNull String name, @NotNull List<String> aliases, @Nullable BiFunction<CommandSender, List<String>, Boolean> executor) {
        this(name, null, aliases, executor);
    }

    public SubcmdExecutor(@NotNull String name, @Nullable String permission) {
        this(name, permission, new ArrayList<>(), null);
    }

    public SubcmdExecutor(@NotNull String name, @Nullable String permission, @Nullable BiFunction<CommandSender, List<String>, Boolean> executor) {
        this(name, permission, new ArrayList<>(), executor);
    }

    public SubcmdExecutor(@NotNull String name, @Nullable String permission, @NotNull List<String> aliases) {
        this(name, permission, aliases, null);
    }

    public SubcmdExecutor(@NotNull String name, @Nullable String permission, @NotNull List<String> aliases, @Nullable BiFunction<CommandSender, List<String>, Boolean> executor) {
        this.name = name;
        this.permission = permission;
        this.aliases = aliases;
        this.executor = executor;
        this.subcommands = new ConcurrentHashMap<>();
    }

    @Override
    @Nullable
    public BiFunction<CommandSender, List<String>, Boolean> executor() {
        return executor;
    }

    @Override
    public ISubcmdExecutor setExecutor(@Nullable BiFunction<CommandSender, List<String>, Boolean> executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public @NotNull List<String> tabArgs() {
        if (tabArgsSupplier == null)
            return new ArrayList<>();
        return tabArgsSupplier.get();
    }

    @Override
    @NotNull
    public ISubcmdExecutor setTabArgsSupplier(@NotNull Supplier<List<String>> tabArgumentsSupplier) {
        this.tabArgsSupplier = tabArgumentsSupplier;
        return this;
    }

    @Override
    public Supplier<List<String>> tabArgsSupplier() {
        return tabArgsSupplier;
    }

    /**
     * 获取此子命令的名字
     *
     * @return 子命令的名字
     */
    @Override
    @NotNull
    public String name() {
        return name;
    }

    /**
     * 获取此子命令的别名
     *
     * @return 子命令的别名
     */
    @Override
    @NotNull
    public List<String> aliases() {
        return aliases;
    }

    @Override
    public ISubcmdExecutor setAliases(@NotNull List<String> aliases) {
        this.aliases = aliases;
        return this;
    }

    @Override
    public ISubcmdExecutor addAliases(@NotNull String alias) {
        this.aliases.add(alias);
        return this;
    }

    /**
     * 获取此子命令所需的权限,如果不需要权限则返回null
     *
     * @return 子命令所需权限
     */
    @Override
    @Nullable
    public String permission() {
        return permission;
    }

    @Override
    public ISubcmdExecutor setPermission(@Nullable String permission) {
        this.permission = permission;
        return this;
    }

    @Override
    @NotNull
    public Map<String, ISubcmdExecutor> subcommands() {
        return subcommands;
    }

}
