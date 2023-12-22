package crypticlib.command.impl;

import crypticlib.CrypticLib;
import crypticlib.command.CommandInfo;
import crypticlib.command.ICmdExecutor;
import crypticlib.command.IRootCmdExecutor;
import crypticlib.command.ISubcmdExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * CrypticLib提供的插件基础命令接口
 */
public class RootCmdExecutor implements ICmdExecutor, IRootCmdExecutor {

    private final Map<String, ISubcmdExecutor> subcommands;
    private Supplier<List<String>> tabCompleter;
    private BiFunction<CommandSender, List<String>, Boolean> executor;
    private Boolean registered;

    public RootCmdExecutor() {
        this(null);
    }

    public RootCmdExecutor(BiFunction<CommandSender, List<String>, Boolean> executor) {
        this.subcommands = new ConcurrentHashMap<>();
        this.executor = executor;
        this.registered = false;
    }

    @Override
    @NotNull
    public Map<String, ISubcmdExecutor> subcommands() {
        return subcommands;
    }

    @Override
    @Nullable
    public BiFunction<CommandSender, List<String>, Boolean> executor() {
        return executor;
    }

    @Override
    public IRootCmdExecutor setExecutor(@Nullable BiFunction<CommandSender, List<String>, Boolean> executor) {
        this.executor = executor;
        return this;
    }


    @Override
    public Supplier<List<String>> tabCompleter() {
        return tabCompleter;
    }

    @Override
    @NotNull
    public IRootCmdExecutor setTabCompleter(@NotNull Supplier<List<String>> tabCompleter) {
        this.tabCompleter = tabCompleter;
        return this;
    }

    @Override
    public void register(@NotNull Plugin plugin, @NotNull CommandInfo commandInfo) {
        if (registered)
            throw new UnsupportedOperationException("Cannot register a command repeatedly");
        registered = true;
        CrypticLib.commandManager().register(plugin, commandInfo, this);
    }

}
