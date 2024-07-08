package crypticlib.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 命令管理器
 * @param <Plugin> 插件主类
 * @param <Executor> 命令执行器类
 * @param <Command> 命令类
 */
public interface CommandManager<Plugin, Executor, Command> {

    Command register(@NotNull Plugin plugin, @NotNull CommandInfo commandInfo, @NotNull Executor commandExecutor);

    @Nullable
    Command unregister(Plugin plugin, String commandName);

    void unregisterAll();

    Map<String, Map<String, Command>> registeredCommands();

}
