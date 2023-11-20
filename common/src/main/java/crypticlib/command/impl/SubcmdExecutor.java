package crypticlib.command.impl;

import crypticlib.command.api.ICmdExecutor;
import crypticlib.command.api.ISubcmdExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * CrypticLib提供的子命令接口
 */
public class SubcmdExecutor implements ISubcmdExecutor {

    private final String name;
    private String permission;
    private List<String> aliases;
    private List<String> tabArguments;
    private BiFunction<CommandSender, List<String>, Boolean> executor;
    private final Map<String, ISubcmdExecutor> subcommands;

    public SubcmdExecutor(String name) {
        this(name, null, new ArrayList<>());
    }

    public SubcmdExecutor(String name, BiFunction<CommandSender, List<String>, Boolean> executor) {
        this(name, null, new ArrayList<>(), executor);
    }

    public SubcmdExecutor(String name, List<String> aliases) {
        this(name, null, aliases, null);
    }

    public SubcmdExecutor(String name, List<String> aliases, BiFunction<CommandSender, List<String>, Boolean> executor) {
        this(name, null, aliases, executor);
    }

    public SubcmdExecutor(String name, String permission) {
        this(name, permission, new ArrayList<>(), null);
    }

    public SubcmdExecutor(String name, String permission, BiFunction<CommandSender, List<String>, Boolean> executor) {
        this(name, permission, new ArrayList<>(), executor);
    }

    public SubcmdExecutor(String name, String permission, List<String> aliases) {
        this(name, permission, aliases, null);
    }

    public SubcmdExecutor(String name, String permission, List<String> aliases, BiFunction<CommandSender, List<String>, Boolean> executor) {
        this.name = name;
        this.permission = permission;
        this.aliases = aliases;
        this.executor = executor;
        this.subcommands = new ConcurrentHashMap<>();
        this.tabArguments = new ArrayList<>();
    }

    @Override
    public BiFunction<CommandSender, List<String>, Boolean> executor() {
        return executor;
    }

    @Override
    public ISubcmdExecutor setExecutor(BiFunction<CommandSender, List<String>, Boolean> executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public @NotNull List<String> tabArguments() {
        return tabArguments;
    }

    @Override
    public ISubcmdExecutor setTabArguments(List<String> tabArguments) {
        this.tabArguments = tabArguments;
        return this;
    }

    /**
     * 获取此子命令的名字
     * @return 子命令的名字
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * 获取此子命令的别名
     * @return 子命令的别名
     */
    @Override
    public List<String> aliases() {
        return aliases;
    }

    @Override
    public ISubcmdExecutor setAliases(List<String> aliases) {
        this.aliases = aliases;
        return this;
    }

    @Override
    public ISubcmdExecutor addAliases(String alias) {
        this.aliases.add(alias);
        return this;
    }

    /**
     * 获取此子命令所需的权限,如果不需要权限则返回null
     * @return 子命令所需权限
     */
    @Override
    public String permission() {
        return permission;
    }

    @Override
    public ISubcmdExecutor setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    @Override
    public @NotNull Map<String, ISubcmdExecutor> subcommands() {
        return subcommands;
    }

}
