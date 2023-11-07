package crypticlib.command;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * CrypticLib提供的子命令接口
 */
public interface ISubCmdExecutor extends ICmdExecutor {

    /**
     * 执行此子命令
     * @param sender 发送此命令的人
     * @param args 发送时的参数
     * @return 执行结果
     */
    default boolean onCommand(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            return false;
        }
        ISubCmdExecutor subCommand = subCommands().get(args.get(0));
        if (subCommand != null) {
            String perm = subCommand.permission();
            if (perm == null || sender.hasPermission(perm)) {
                return subCommand.onCommand(sender, args.subList(1, args.size()));
            }
        }
        return false;
    }

    /**
     * 获取此子命令的名字
     * @return 子命令的名字
     */
    String subCommandName();

    /**
     * 获取此子命令所需的权限
     * @return 子命令所需权限
     */
    String permission();

}
