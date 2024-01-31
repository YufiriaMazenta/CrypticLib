package crypticlib.command;

import crypticlib.perm.PermDef;
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
public class SubcmdExecutor implements ICmdExecutor {

    private final String name;
    private final Map<String, SubcmdExecutor> subcommands;
    private String permission;
    private PermDef permDef = PermDef.OP;
    private List<String> aliases;
    private BiFunction<CommandSender, List<String>, List<String>> tabCompleter;
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
    public SubcmdExecutor setExecutor(@Nullable BiFunction<CommandSender, List<String>, Boolean> executor) {
        this.executor = executor;
        return this;
    }

    @Override
    @NotNull
    public SubcmdExecutor setTabCompleter(@NotNull BiFunction<CommandSender, List<String>, List<String>> tabCompleter) {
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
        return name;
    }

    /**
     * 获取此子命令的别名
     *
     * @return 子命令的别名
     */
    @NotNull
    public List<String> aliases() {
        return aliases;
    }

    public SubcmdExecutor setAliases(@NotNull List<String> aliases) {
        this.aliases = aliases;
        return this;
    }

    public SubcmdExecutor addAliases(@NotNull String alias) {
        this.aliases.add(alias);
        return this;
    }

    /**
     * 获取此子命令所需的权限,如果不需要权限则返回null
     *
     * @return 子命令所需权限
     */
    @Nullable
    public String permission() {
        return permission;
    }

    public SubcmdExecutor setPermission(@Nullable String permission) {
        this.permission = permission;
        return this;
    }

    @Override
    public @NotNull Map<String, SubcmdExecutor> subcommands() {
        return subcommands;
    }

    @Override
    public SubcmdExecutor regSub(@NotNull SubcmdExecutor subcmdExecutor) {
        return (SubcmdExecutor) ICmdExecutor.super.regSub(subcmdExecutor);
    }

    @Override
    public SubcmdExecutor regSub(@NotNull String name, @NotNull BiFunction<CommandSender, List<String>, Boolean> executor) {
        return (SubcmdExecutor) ICmdExecutor.super.regSub(name, executor);
    }

    public @NotNull PermDef permDef() {
        return permDef;
    }

    public SubcmdExecutor setPermDef(@NotNull PermDef permDef) {
        this.permDef = permDef;
        return this;
    }

}
