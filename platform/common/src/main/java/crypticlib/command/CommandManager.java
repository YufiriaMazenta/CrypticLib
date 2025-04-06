package crypticlib.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 命令管理器
 * @param <Executor> 命令执行器类
 * @param <Command> 平台的命令类
 */
public interface CommandManager<Executor, Command> {

    /**
     * 往服务器中注册一个命令，并返回注册完成的命令
     * @param commandInfo 注册命令的信息
     * @param commandExecutor 注册命令的执行器
     * @return 注册完成的命令
     */
    Command register(@NotNull CommandInfo commandInfo, @NotNull Executor commandExecutor);

    /**
     * 注册一个CrypticLib根命令到服务器中,并返回注册完成的命令
     * @param commandTree 要注册的命令
     * @return 注册完成的命令
     */
    Command register(CommandTree commandTree);

    /**
     * 从服务器中注销一个命令，并返回注销掉的命令，若命令不存在，将会返回null
     * @param commandName 要注销的命令
     * @return
     */
    @Nullable
    Command unregister(String commandName);

    /**
     * 注销掉所有通过此接口注册的命令
     */
    void unregisterAll();

    /**
     * 获取所有通过此接口注册的命令
     */
    Map<String, Command> registeredCommands();

}
