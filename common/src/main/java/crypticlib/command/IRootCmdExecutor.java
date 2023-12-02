package crypticlib.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public interface IRootCmdExecutor extends TabExecutor, ICmdExecutor {

    IRootCmdExecutor setExecutor(@Nullable BiFunction<CommandSender, List<String>, Boolean> executor);

    @Override
    default List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return onTabComplete(sender, Arrays.asList(args));
    }

    @Override
    default boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return onCommand(sender, Arrays.asList(args));
    }

    @Override
    default IRootCmdExecutor regSub(@NotNull ISubcmdExecutor subcmdExecutor) {
        return (IRootCmdExecutor) ICmdExecutor.super.regSub(subcmdExecutor);
    }

    @Override
    default IRootCmdExecutor regSub(@NotNull String name, @NotNull BiFunction<CommandSender, List<String>, Boolean> executor) {
        return (IRootCmdExecutor) ICmdExecutor.super.regSub(name, executor);
    }

    @Override
    @NotNull
    IRootCmdExecutor setTabArguments(@NotNull List<String> tabArguments);

    @Override
    default IRootCmdExecutor addTabArguments(@NotNull String tabArgument) {
        return (IRootCmdExecutor) ICmdExecutor.super.addTabArguments(tabArgument);
    }

    void register(@NotNull Plugin plugin, @NotNull CommandInfo commandInfo);

}
