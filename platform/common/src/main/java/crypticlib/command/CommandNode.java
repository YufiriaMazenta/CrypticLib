package crypticlib.command;

import crypticlib.Invoker;
import crypticlib.command.annotation.Subcommand;
import crypticlib.perm.PermInfo;
import crypticlib.util.ReflectionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

/**
 * CrypticLib提供的命令节点类,用于注册子命令
 */
public class CommandNode {

    protected final Map<String, CommandNode> subcommands = new HashMap<>();
    protected final CommandInfo commandInfo;

    public CommandNode(@NotNull CommandInfo commandInfo) {
        this.commandInfo = commandInfo;
    }

    public CommandNode(@NotNull String name) {
        this(name, null, new ArrayList<>());
    }

    public CommandNode(@NotNull String name, @NotNull List<String> aliases) {
        this(name, null, aliases);
    }

    public CommandNode(@NotNull String name, @Nullable PermInfo permission) {
        this(name, permission, new ArrayList<>());
    }

    public CommandNode(@NotNull String name, @Nullable PermInfo permission, @NotNull List<String> aliases) {
        this.commandInfo = new CommandInfo(name, permission, aliases);
    }

    /**
     * 命令默认执行的方法，当未输入参数或者没有子命令时执行
     *
     * @param invoker 执行者
     * @param args   参数
     */
    public void execute(@NotNull Invoker invoker, @NotNull List<String> args) {
        sendDescriptions(invoker);
    }

    /**
     * 当执行者无权限时执行的方法，默认不进行任何操作
     * @param invoker 命令执行者
     * @param args 参数
     */
    public void onNoPerm(@NotNull Invoker invoker, @NotNull List<String> args) {}

    /**
     * 当命令补全时执行的方法，最终的补全内容会与命令的子命令叠加
     * @return 此命令的补全参数内容
     */
    public @Nullable List<String> tabComplete(@NotNull Invoker invoker, @NotNull List<String> args) {
        return null;
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
    public List<String> toDescriptions(Invoker invoker) {
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
        subcommands.forEach(
            (key, subcommand) -> {
                if (!subcommand.hasPermission(invoker)) {
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

    public void sendDescriptions(Invoker invoker) {
        List<String> descriptions = toDescriptions(invoker);
        for (String description : descriptions) {
            invoker.sendMsg(description);
        }
    }

    /**
     * 检查执行者是否有权限
     * @param invoker 执行者
     * @return 是否有此命令节点的权限
     */
    public boolean hasPermission(Invoker invoker) {
        PermInfo permission = commandInfo().permission();
        if (permission == null)
            return true;
        return permission.hasPermission(invoker);
    }


    /**
     * 注册一条新的子命令，注册相同的子命令会按照注册顺序最后注册的生效
     *
     * @param commandHandler 注册的命令
     */
    public CommandNode regSub(@NotNull CommandNode commandHandler) {
        subcommands.put(commandHandler.commandInfo().name(), commandHandler);
        for (String alias : commandHandler.commandInfo().aliases()) {
            subcommands.put(alias, commandHandler);
        }
        return this;
    }

    /**
     * 命令的子命令表
     *
     * @return 命令的子命令表
     */
    public final @NotNull Map<String, CommandNode> subcommands() {
        return subcommands;
    }

    /**
     * 执行此命令
     *
     * @param invoker 发送此命令的人
     * @param args   发送时的参数
     */
    public final void onCommand(Invoker invoker, List<String> args) {
        //当不存在参数或者参数无法找到对应子命令时，执行自身的执行器
        if (args.isEmpty() || subcommands.isEmpty() || !subcommands.containsKey(args.get(0))) {
            if (hasPermission(invoker)) {
                execute(invoker, args);
            } else {
                onNoPerm(invoker, args);
            }
            return;
        }
        //执行对应的子命令
        CommandNode commandHandler = subcommands.get(args.get(0));
        if (commandHandler != null) {
            commandHandler.onCommand(invoker, args.subList(1, args.size()));
        }
    }

    /**
     * 提供当玩家或控制台按下TAB时返回的内容
     *
     * @param invoker 按下TAB的玩家或者控制台
     * @param args   参数列表
     * @return 返回的tab列表内容
     */
    public final List<String> onTabComplete(Invoker invoker, List<String> args) {
        List<String> arguments;
        List<String> tab = tabComplete(invoker, args);
        if (tab == null) {
            arguments = new ArrayList<>();
        } else {
            arguments = new ArrayList<>(tab);
        }

        //尝试获取子命令的补全内容
        if (!subcommands.isEmpty()) {
            if (args.size() > 1) {
                CommandNode commandHandler = subcommands.get(args.get(0));
                if (commandHandler != null) {
                    if (commandHandler.hasPermission(invoker)) {
                        return commandHandler.onTabComplete(invoker, args.subList(1, args.size()));
                    } else {
                        return Collections.singletonList("");
                    }
                }
                return Collections.singletonList("");
            }
            for (String arg : subcommands.keySet()) {
                CommandNode commandHandler = subcommands.get(arg);
                if (commandHandler.hasPermission(invoker)) {
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

    public final void registerPerms() {
        //扫描子命令,注册子命令所需权限
        for (CommandNode commandTreeNode : subcommands.values()) {
            commandTreeNode.registerPerms();
        }
        //注册自己的权限节点
        PermInfo permission = commandInfo().permission();
        if (permission != null)
            permission.register();
    }

    public final void scanSubCommands() {
        //先注册自己的子命令
        for (Field field : this.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(Subcommand.class))
                continue;
            if (CommandNode.class.isAssignableFrom(field.getType())) {
                CommandNode commandHandler = ReflectionHelper.getDeclaredFieldObj(field, this);
                this.regSub(commandHandler);
            }
        }

        //再注册子命令的子命令
        for (CommandNode commandHandler : subcommands.values()) {
            commandHandler.scanSubCommands();
        }
    }

    public final @NotNull CommandInfo commandInfo() {
        return commandInfo;
    }

}
