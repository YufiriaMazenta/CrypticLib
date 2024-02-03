package crypticlib.command;

import crypticlib.CrypticLib;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * CrypticLib提供的插件命令接口
 */
public class RootCmdExecutor implements ICmdExecutor, TabExecutor {

    private final Map<String, SubcmdExecutor> subcommands;
    private BiFunction<CommandSender, List<String>, List<String>> tabCompleter;
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
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return onTabComplete(sender, Arrays.asList(args));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return onCommand(sender, Arrays.asList(args));
    }

    @Override
    public RootCmdExecutor regSub(@NotNull SubcmdExecutor subcmdExecutor) {
        return (RootCmdExecutor) ICmdExecutor.super.regSub(subcmdExecutor);
    }

    @Override
    public RootCmdExecutor regSub(@NotNull String name, @NotNull BiFunction<CommandSender, List<String>, Boolean> executor) {
        return (RootCmdExecutor) ICmdExecutor.super.regSub(name, executor);
    }

    @Override
    public @NotNull Map<String, SubcmdExecutor> subcommands() {
        return subcommands;
    }

    @Override
    @Nullable
    public BiFunction<CommandSender, List<String>, Boolean> executor() {
        return executor;
    }

    @Override
    public RootCmdExecutor setExecutor(@Nullable BiFunction<CommandSender, List<String>, Boolean> executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public @NotNull BiFunction<CommandSender, List<String>, List<String>> tabCompleter() {
        return tabCompleter;
    }

    @Override
    @NotNull
    public RootCmdExecutor setTabCompleter(@NotNull BiFunction<CommandSender, List<String>, List<String>> tabCompleter) {
        this.tabCompleter = tabCompleter;
        return this;
    }
    
    public void register(@NotNull Plugin plugin, @NotNull CommandInfo commandInfo) {
        if (registered)
            throw new UnsupportedOperationException("Cannot register a command repeatedly");
        registered = true;
        CrypticLib.commandManager().register(plugin, commandInfo, this);
    }

}
