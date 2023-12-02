package crypticlib.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

public interface ISubcmdExecutor extends ICmdExecutor {

    @NotNull String name();

    @NotNull List<String> aliases();

    ISubcmdExecutor setAliases(@NotNull List<String> aliases);

    ISubcmdExecutor addAliases(@NotNull String alias);

    @Override
    @NotNull
    ISubcmdExecutor setTabArguments(@NotNull List<String> tabArguments);

    @Override
    default ISubcmdExecutor addTabArguments(@NotNull String tabArgument) {
        return (ISubcmdExecutor) ICmdExecutor.super.addTabArguments(tabArgument);
    }

    @Nullable String permission();

    ISubcmdExecutor setPermission(@Nullable String permission);

    @Override
    ISubcmdExecutor setExecutor(BiFunction<CommandSender, List<String>, Boolean> executor);

    @Override
    default ISubcmdExecutor regSub(@NotNull ISubcmdExecutor subcmdExecutor) {
        return (ISubcmdExecutor) ICmdExecutor.super.regSub(subcmdExecutor);
    }

    @Override
    default ISubcmdExecutor regSub(@NotNull String name, @NotNull BiFunction<CommandSender, List<String>, Boolean> executor) {
        return (ISubcmdExecutor) ICmdExecutor.super.regSub(name, executor);
    }

}
