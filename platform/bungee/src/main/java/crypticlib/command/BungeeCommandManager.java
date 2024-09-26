package crypticlib.command;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum BungeeCommandManager implements CommandManager<Plugin, BungeeCommand, Command> {

    INSTANCE;

    private final Map<String, Command> registeredCommands = new ConcurrentHashMap<>();

    BungeeCommandManager() {}

    @Override
    public @NotNull Command register(@NotNull Plugin plugin, @NotNull CommandInfo commandInfo, @NotNull BungeeCommand commandExecutor) {
        plugin.getProxy().getPluginManager().registerCommand(plugin, commandExecutor);
        registeredCommands.put(commandInfo.name(), commandExecutor);
        return commandExecutor;
    }

    /**
     * 注销一个命令
     * @param commandName 命令的名字
     * @return 被注销的命令，若为null即不存在此命令
     */
    @Override
    public Command unregister(String commandName) {
        Command command = registeredCommands.get(commandName);
        if (command == null)
            return null;
        ProxyServer.getInstance().getPluginManager().unregisterCommand(command);
        registeredCommands.remove(commandName);
        return command;
    }

    /**
     * 注销所有通过CommandManager的命令
     */
    @Override
    public void unregisterAll() {
        registeredCommands.forEach(
            (commandName, command) -> {
                ProxyServer.getInstance().getPluginManager().unregisterCommand(command);
            }
        );
        registeredCommands.clear();
    }

    @Override
    public Map<String, Command> registeredCommands() {
        return registeredCommands;
    }

}
