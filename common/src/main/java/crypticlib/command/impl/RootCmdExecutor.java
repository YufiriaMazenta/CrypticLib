package crypticlib.command.impl;

import crypticlib.CrypticLib;
import crypticlib.command.CommandInfo;
import crypticlib.command.ICmdExecutor;
import crypticlib.command.IRootCmdExecutor;
import crypticlib.command.ISubcmdExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * CrypticLib提供的插件基础命令接口
 */
public class RootCmdExecutor implements ICmdExecutor, IRootCmdExecutor {

    private final Map<String, ISubcmdExecutor> subcommands;
    private List<String> tabArguments;
    private BiFunction<CommandSender, List<String>, Boolean> executor;
    private boolean registered;

    public RootCmdExecutor() {
        this(null);
    }

    public RootCmdExecutor(BiFunction<CommandSender, List<String>, Boolean> executor) {
        this.subcommands = new ConcurrentHashMap<>();
        this.executor = executor;
        this.registered = false;
        this.tabArguments = new ArrayList<>();
    }

    @Override
    public @NotNull Map<String, ISubcmdExecutor> subcommands() {
        return subcommands;
    }

    @Override
    public BiFunction<CommandSender, List<String>, Boolean> executor() {
        return executor;
    }

    @Override
    public IRootCmdExecutor setExecutor(BiFunction<CommandSender, List<String>, Boolean> executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public IRootCmdExecutor setTabArguments(List<String> tabArguments) {
        this.tabArguments = tabArguments;
        return this;
    }

    @Override
    public IRootCmdExecutor addTabArguments(String tabArgument) {
        return IRootCmdExecutor.super.addTabArguments(tabArgument);
    }

    @Override
    public @NotNull List<String> tabArguments() {
        return this.tabArguments;
    }

    @Override
    public void register(Plugin plugin, CommandInfo commandInfo) {
        if (registered)
            throw new UnsupportedOperationException("Cannot register a command repeatedly");
        registered = true;
        CrypticLib.commandManager().register(plugin, commandInfo, this);
    }

}
