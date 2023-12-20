package crypticlib.command;

import crypticlib.command.impl.SubcmdExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

/**
 * CrypticLib提供的命令接口，简便了对于Tab返回的编写
 */
public interface ICmdExecutor {

    /**
     * 获得子命令表
     *
     * @return 命令的子命令表
     */
    @NotNull
    default Map<String, ISubcmdExecutor> subcommands() {
        return new HashMap<>();
    }

    /**
     * 注册一条新的子命令，注册相同的子命令会按照注册顺序最后注册的生效
     *
     * @param subcmdExecutor 注册的命令
     */
    default ICmdExecutor regSub(@NotNull ISubcmdExecutor subcmdExecutor) {
        subcommands().put(subcmdExecutor.name(), subcmdExecutor);
        for (String alias : subcmdExecutor.aliases()) {
            subcommands().put(alias, subcmdExecutor);
        }
        return this;
    }

    /**
     * 注册一条新的子命令，注册相同的子命令会按照注册顺序最后注册的生效
     *
     * @param name     子命令的名字
     * @param executor 子命令的执行方法
     */
    default ICmdExecutor regSub(@NotNull String name, @NotNull BiFunction<CommandSender, List<String>, Boolean> executor) {
        SubcmdExecutor subcmdExecutor = new SubcmdExecutor(name, executor);
        regSub(subcmdExecutor);
        return this;
    }

    /**
     * 命令默认执行的方法，当未输入参数或者没有子命令时执行
     *
     * @param sender 执行者
     * @param args   参数
     */
    default Boolean execute(CommandSender sender, List<String> args) {
        if (executor() != null)
            return executor().apply(sender, args);
        return true;
    }

    @Nullable
    BiFunction<CommandSender, List<String>, Boolean> executor();

    ICmdExecutor setExecutor(@Nullable BiFunction<CommandSender, List<String>, Boolean> executor);

    /**
     * 执行此子命令
     *
     * @param sender 发送此命令的人
     * @param args   发送时的参数
     * @return 执行结果
     */
    default Boolean onCommand(CommandSender sender, List<String> args) {
        if (args.isEmpty() || subcommands().isEmpty() || !subcommands().containsKey(args.get(0))) {
            return execute(sender, args);
        }
        ISubcmdExecutor subCommand = subcommands().get(args.get(0));
        if (subCommand != null) {
            String perm = subCommand.permission();
            if (perm == null || sender.hasPermission(perm)) {
                return subCommand.onCommand(sender, args.subList(1, args.size()));
            }
        }
        return true;
    }

    @NotNull
    default List<String> tabArguments() {
        return new ArrayList<>();
    }

    @NotNull
    ICmdExecutor setTabArguments(@NotNull List<String> tabArguments);

    default ICmdExecutor addTabArguments(@NotNull String tabArgument) {
        tabArguments().add(tabArgument);
        return this;
    }

    /**
     * 提供当玩家或控制台按下TAB时返回的内容
     *
     * @param sender 按下TAB的玩家或者控制台
     * @param args   参数列表
     * @return 返回的tab列表内容
     */
    default List<String> onTabComplete(CommandSender sender, List<String> args) {
        List<String> arguments = new ArrayList<>(tabArguments());
        if (!subcommands().isEmpty()) {
            if (args.size() > 1) {
                ISubcmdExecutor subCommand = subcommands().get(args.get(0));
                if (subCommand != null) {
                    String permission = subCommand.permission();
                    if (permission != null) {
                        if (sender.hasPermission(permission))
                            return subCommand.onTabComplete(sender, args.subList(1, args.size()));
                    }
                }
                return Collections.singletonList("");

            }
            for (String subCmd : subcommands().keySet()) {
                ISubcmdExecutor subCommand = subcommands().get(subCmd);
                String permission = subCommand.permission();
                if (permission != null) {
                    if (sender.hasPermission(permission))
                        arguments.add(subCmd);
                } else {
                    arguments.add(subCmd);
                }
            }
        }
        if (arguments.isEmpty())
            return null;
        arguments.removeIf(str -> !str.contains(args.get(0)));
        return arguments;

    }

}
