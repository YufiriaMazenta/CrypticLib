package crypticlib.command;

import crypticlib.command.annotation.Subcommand;
import crypticlib.perm.PermInfo;
import crypticlib.util.ReflectUtil;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;

/**
 * CrypticLib提供的底层命令接口
 */
public interface ICommandHandler {

    /**
     * 命令的子命令表
     *
     * @return 命令的子命令表
     */
    default @NotNull Map<String, SubcommandHandler> subcommands() {
        return new HashMap<>();
    }

    /**
     * 注册一条新的子命令，注册相同的子命令会按照注册顺序最后注册的生效
     *
     * @param subcommandHandler 注册的命令
     */
    default ICommandHandler regSub(@NotNull SubcommandHandler subcommandHandler) {
        subcommands().put(subcommandHandler.name(), subcommandHandler);
        for (String alias : subcommandHandler.aliases()) {
            subcommands().put(alias, subcommandHandler);
        }
        return this;
    }

    /**
     * 注册一条新的子命令，注册相同的子命令会按照注册顺序最后注册的生效
     *
     * @param name     子命令的名字
     * @param executor 子命令的执行方法
     */
    default ICommandHandler regSub(@NotNull String name, @NotNull BiFunction<CommandSender, List<String>, Boolean> executor) {
        SubcommandHandler commandTreeNode = new SubcommandHandler(name, executor);
        regSub(commandTreeNode);
        return this;
    }

    /**
     * 命令默认执行的方法，当未输入参数或者没有子命令时执行
     *
     * @param sender 执行者
     * @param args   参数
     */
    default boolean execute(CommandSender sender, List<String> args) {
        if (executor() != null)
            return executor().apply(sender, args);
        return true;
    }

    /**
     * 命令的执行器
     * @return 此命令的执行器
     */
    @Nullable
    BiFunction<CommandSender, List<String>, Boolean> executor();

    /**
     * 设置此命令的执行器
     * @param executor 命令执行器
     */
    ICommandHandler setExecutor(@Nullable BiFunction<CommandSender, List<String>, Boolean> executor);

    /**
     * 执行此命令
     *
     * @param sender 发送此命令的人
     * @param args   发送时的参数
     * @return 执行结果
     */
    default boolean onCommand(CommandSender sender, List<String> args) {
        //当不存在参数或者参数无法找到对应子命令时，执行自身的执行器
        if (args.isEmpty() || subcommands().isEmpty() || !subcommands().containsKey(args.get(0))) {
            return execute(sender, args);
        }
        //执行对应的子命令
        SubcommandHandler subcommand = subcommands().get(args.get(0));
        if (subcommand != null) {
            PermInfo perm = subcommand.permission();
            if (perm == null || sender.hasPermission(perm.permission())) {
                return subcommand.onCommand(sender, args.subList(1, args.size()));
            }
        }
        return true;
    }

    /**
     * 命令的命令补全器
     * @return 此命令的默认返回参数提供者
     */
    @Nullable BiFunction<CommandSender, List<String>, List<String>> tabCompleter();

    /**
     * 命令的补全内容
     * @return 此命令的默认返回参数
     */
    default @NotNull List<String> tabArgs(CommandSender sender, List<String> args) {
        BiFunction<CommandSender, List<String>, List<String>> tabCompleter = tabCompleter();
        if (tabCompleter == null)
            return new ArrayList<>();
        return tabCompleter.apply(sender, args);
    }

    /**
     * 设置此命令的命令补全器
     * @param tabCompleter 此命令的默认返回参数提供者
     */
    @NotNull
    ICommandHandler setTabCompleter(@NotNull BiFunction<CommandSender, List<String>, List<String>> tabCompleter);

    /**
     * 提供当玩家或控制台按下TAB时返回的内容
     *
     * @param sender 按下TAB的玩家或者控制台
     * @param args   参数列表
     * @return 返回的tab列表内容
     */
    default List<String> onTabComplete(CommandSender sender, List<String> args) {
        List<String> arguments = new ArrayList<>(tabArgs(sender, args));
        //尝试获取子命令的补全内容
        if (!subcommands().isEmpty()) {
            if (args.size() > 1) {
                SubcommandHandler subcommand = subcommands().get(args.get(0));
                if (subcommand != null) {
                    PermInfo perm = subcommand.permission();
                    if (perm != null) {
                        if (sender.hasPermission(perm.permission()))
                            return subcommand.onTabComplete(sender, args.subList(1, args.size()));
                        else
                            return Collections.singletonList("");
                    } else {
                        return subcommand.onTabComplete(sender, args.subList(1, args.size()));
                    }
                }
                return Collections.singletonList("");
            }
            for (String arg : subcommands().keySet()) {
                SubcommandHandler subcommand = subcommands().get(arg);
                PermInfo perm = subcommand.permission();
                if (perm != null) {
                    if (sender.hasPermission(perm.permission()))
                        arguments.add(arg);
                } else {
                    arguments.add(arg);
                }
            }
        }
        arguments.removeIf(str -> !str.contains(args.get(0)));
        return arguments;
    }

    default void registerPerms() {
        for (SubcommandHandler commandTreeNode : subcommands().values()) {
            commandTreeNode.registerPerms();
        }
    }

    default void scanNodes() {
        for (SubcommandHandler subcommand : subcommands().values()) {
            for (Field field : subcommand.getClass().getDeclaredFields()) {
                if (!field.isAnnotationPresent(Subcommand.class))
                    continue;
                if (field.getType().equals(SubcommandHandler.class)) {
                    SubcommandHandler commandTreeNode = (SubcommandHandler) ReflectUtil.getDeclaredFieldObj(field, subcommand);
                    subcommand.regSub(commandTreeNode);
                }
            }
            subcommand.scanNodes();
        }
    }

}
