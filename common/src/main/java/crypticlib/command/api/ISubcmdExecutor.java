package crypticlib.command.api;

import crypticlib.command.impl.SubcmdExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.function.BiFunction;

public interface ISubcmdExecutor extends ICmdExecutor {

    String name();

    List<String> aliases();

    String permission();

    @Override
    default ISubcmdExecutor regSub(ISubcmdExecutor subcmdExecutor) {
        return (ISubcmdExecutor) ICmdExecutor.super.regSub(subcmdExecutor);
    }

    @Override
    default ISubcmdExecutor regSub(String name, BiFunction<CommandSender, List<String>, Boolean> executor) {
        return (ISubcmdExecutor) ICmdExecutor.super.regSub(name, executor);
    }

    @Override
    ISubcmdExecutor setExecutor(BiFunction<CommandSender, List<String>, Boolean> executor);

    ISubcmdExecutor setPermission(String permission);

}
