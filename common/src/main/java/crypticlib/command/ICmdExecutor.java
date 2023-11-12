package crypticlib.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * CrypticLib提供的命令接口，简便了对于Tab返回的编写
 */
interface ICmdExecutor {

    /**
     * 获得子命令表
     * @return 命令的子命令表
     */
    default @NotNull Map<String, ISubCmdExecutor> subCommands() {
        return new HashMap<>();
    }

    /**
     * 注册一条新的子命令，注册相同的子命令会按照注册顺序最后注册的生效
     * @param subCommand 注册的命令
     */
    default void regSubCommand(ISubCmdExecutor subCommand) {
        subCommands().put(subCommand.subCommandName(), subCommand);
        for (String alias : subCommand.alias()) {
            subCommands().put(alias, subCommand);
        }
    }

    /**
     * 提供当玩家或控制台按下TAB时返回的内容
     * @param sender 按下TAB的玩家或者控制台
     * @param args 参数列表
     * @return 返回的tab列表内容
     */
    default List<String> onTabComplete(CommandSender sender, List<String> args) {
        if (subCommands().isEmpty())
            return Collections.singletonList("");
        if (args.size() <= 1) {
            List<String> tabList = new ArrayList<>();
            for (String subCmd : subCommands().keySet()) {
                ISubCmdExecutor subCommand = subCommands().get(subCmd);
                if (subCommand.permission() != null) {
                    if (sender.hasPermission(subCommand.permission()))
                        tabList.add(subCmd);
                } else {
                    tabList.add(subCmd);
                }
            }
            tabList.removeIf(str -> !str.startsWith(args.get(0)));
            return tabList;
        }
        ISubCmdExecutor subCommand = subCommands().get(args.get(0));
        if (subCommand != null) {
            if (subCommand.permission() != null) {
                if (!sender.hasPermission(subCommand.permission()))
                    return Collections.singletonList("");
            }
            return subCommand.onTabComplete(sender, args.subList(1, args.size()));
        }
        else
            return Collections.singletonList("");
    }

}
