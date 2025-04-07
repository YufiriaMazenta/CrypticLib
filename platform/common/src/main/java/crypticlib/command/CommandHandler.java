package crypticlib.command;

import crypticlib.command.annotation.Subcommand;
import crypticlib.perm.PermInfo;
import crypticlib.util.IOHelper;
import crypticlib.util.ReflectionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

/**
 * CrypticLib提供的底层命令接口
 */
public interface CommandHandler {

    /**
     * 命令默认执行的方法，当未输入参数或者没有子命令时执行
     *
     * @param sender 执行者
     * @param args   参数
     */
    default void execute(@NotNull CommandInvoker sender, @NotNull List<String> args) {
        sendDescriptions(sender);
    }

    /**
     * 当命令补全时执行的方法，最终的补全内容会与命令的子命令叠加
     * @return 此命令的补全参数内容
     */
    default @Nullable List<String> tab(@NotNull CommandInvoker sender, @NotNull List<String> args) {
        return null;
    }

    /**
     * 命令的子命令表
     *
     * @return 命令的子命令表
     */
    default @NotNull Map<String, CommandNode> subcommands() {
        return new HashMap<>();
    }

    /**
     * 注册一条新的子命令，注册相同的子命令会按照注册顺序最后注册的生效
     *
     * @param commandNode 注册的命令
     */
    default CommandHandler regSub(@NotNull CommandNode commandNode) {
        subcommands().put(commandNode.commandInfo().name(), commandNode);
        for (String alias : commandNode.commandInfo().aliases()) {
            subcommands().put(alias, commandNode);
        }
        return this;
    }

    /**
     * 执行此命令
     *
     * @param sender 发送此命令的人
     * @param args   发送时的参数
     */
    default void onCommand(CommandInvoker sender, List<String> args) {
        //当不存在参数或者参数无法找到对应子命令时，执行自身的执行器
        if (args.isEmpty() || subcommands().isEmpty() || !subcommands().containsKey(args.get(0))) {
            execute(sender, args);
            return;
        }
        //执行对应的子命令
        CommandNode commandNode = subcommands().get(args.get(0));
        if (commandNode != null && commandNode.hasPermission(sender)) {
            commandNode.onCommand(sender, args.subList(1, args.size()));
        }
    }

    /**
     * 提供当玩家或控制台按下TAB时返回的内容
     *
     * @param sender 按下TAB的玩家或者控制台
     * @param args   参数列表
     * @return 返回的tab列表内容
     */
    default List<String> onTabComplete(CommandInvoker sender, List<String> args) {
        List<String> arguments;
        List<String> tab = tab(sender, args);
        if (tab == null) {
            arguments = new ArrayList<>();
        } else {
            arguments = new ArrayList<>(tab);
        }

        //尝试获取子命令的补全内容
        if (!subcommands().isEmpty()) {
            if (args.size() > 1) {
                CommandNode commandNode = subcommands().get(args.get(0));
                if (commandNode != null) {
                    if (commandNode.hasPermission(sender)) {
                        return commandNode.onTabComplete(sender, args.subList(1, args.size()));
                    } else {
                        return Collections.singletonList("");
                    }
                }
                return Collections.singletonList("");
            }
            for (String arg : subcommands().keySet()) {
                CommandNode commandNode = subcommands().get(arg);
                if (commandNode.hasPermission(sender)) {
                    arguments.add(arg);
                }
            }
        }
        if (!args.isEmpty())
            arguments.removeIf(str -> !str.contains(args.get(args.size() - 1)));
        if (arguments.isEmpty())
            return Collections.singletonList("");
        return arguments;
    }

    default void registerPerms() {
        //扫描子命令,注册子命令所需权限
        for (CommandNode commandTreeNode : subcommands().values()) {
            commandTreeNode.registerPerms();
        }
        //注册自己的权限节点
        PermInfo permission = commandInfo().permission();
        if (permission != null)
            permission.register();
    }

    default void scanSubCommands() {
        //先注册自己的子命令
        for (Field field : this.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(Subcommand.class))
                continue;
            if (CommandNode.class.isAssignableFrom(field.getType())) {
                CommandNode commandNode = ReflectionHelper.getDeclaredFieldObj(field, this);
                this.regSub(commandNode);
            }
        }

        //再注册子命令的子命令
        for (CommandNode commandNode : subcommands().values()) {
            commandNode.scanSubCommands();
        }
    }

    /**
     * 获取此命令的介绍
     * 格式为:
     * <command>:
     * <usage>
     * <description>
     *  - <子命令>
     *    <子命令usage>
     *    <子命令description>
     *  - <子命令>
     *    <子命令usage>
     *    <子命令description>
     *
     * @return 转换完成的介绍
     */
    default List<String> toDescriptions(CommandInvoker CommandInvoker) {
        List<String> description = new ArrayList<>();

        StringJoiner nameJoiner = new StringJoiner(" | ", "&7", ":");
        nameJoiner.add(commandInfo().name());
        for (String alias : commandInfo().aliases()) {
            nameJoiner.add(alias);
        }
        description.add(nameJoiner.toString());
        String usage = commandInfo().usage();
        if (usage != null && !usage.isEmpty()) {
            description.add(usage);
        }
        String desc = commandInfo().description();
        if (desc != null && !desc.isEmpty()) {
            description.add(desc);
        }
        subcommands().forEach(
            (key, subcommand) -> {
                if (!subcommand.hasPermission(CommandInvoker)) {
                    return;
                }
                StringJoiner subNameJoiner = new StringJoiner(" | ", " &7- ", "");
                subNameJoiner.add(subcommand.commandInfo().name());
                for (String alias : subcommand.commandInfo().aliases()) {
                    subNameJoiner.add(alias);
                }
                description.add(subNameJoiner.toString());
                String subUsage = subcommand.commandInfo().usage();
                if (subUsage != null && !subUsage.isEmpty()) {
                    description.add("   " + subUsage);
                }
                String subDesc = subcommand.commandInfo().description();
                if (subDesc == null || subDesc.isEmpty()) {
                    return;
                }
                description.add("   " + subDesc);
            }
        );
        return description;
    }

    void sendDescriptions(CommandInvoker CommandInvoker);

    @NotNull CommandInfo commandInfo();

    /**
     * 检查执行者是否有权限
     * @param invoker 执行者
     * @return 是否有此命令节点的权限
     */
    default boolean hasPermission(CommandInvoker invoker) {
        PermInfo permission = commandInfo().permission();
        if (permission == null)
            return true;
        return permission.hasPermission(invoker);
    }

}
