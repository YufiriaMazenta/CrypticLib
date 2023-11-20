package crypticlib.command.api;

import crypticlib.command.impl.SubcmdExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.function.BiFunction;

public interface ISubcmdExecutor extends ICmdExecutor {

    String name();

    List<String> aliases();

    ISubcmdExecutor setAliases(List<String> aliases);

    ISubcmdExecutor addAliases(String alias);

    @Override
    ISubcmdExecutor setTabArguments(List<String> tabArguments);

    @Override
    default ISubcmdExecutor addTabArguments(String tabArgument) {
        return (ISubcmdExecutor) ICmdExecutor.super.addTabArguments(tabArgument);
    }

    String permission();

    ISubcmdExecutor setPermission(String permission);

    @Override
    ISubcmdExecutor setExecutor(BiFunction<CommandSender, List<String>, Boolean> executor);

    @Override
    default ISubcmdExecutor regSub(ISubcmdExecutor subcmdExecutor) {
        return (ISubcmdExecutor) ICmdExecutor.super.regSub(subcmdExecutor);
    }

    @Override
    default ISubcmdExecutor regSub(String name, BiFunction<CommandSender, List<String>, Boolean> executor) {
        return (ISubcmdExecutor) ICmdExecutor.super.regSub(name, executor);
    }

}
