package crypticlib.command;

import crypticlib.CrypticLibPlugin;
import crypticlib.PlatformSide;
import crypticlib.lifecycle.LifecycleTaskSettings;
import crypticlib.lifecycle.LifecycleTask;
import crypticlib.lifecycle.Lifecycle;
import crypticlib.lifecycle.LifecycleRule;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@LifecycleTaskSettings(
    rules = @LifecycleRule(lifeCycle = Lifecycle.INIT),
    platforms = PlatformSide.BUNGEE
)
public enum BungeeCommandManager implements CommandManager<Command, Command>, LifecycleTask {

    INSTANCE;

    private final Map<String, Command> registeredCommands = new ConcurrentHashMap<>();
    private Plugin pluginInstance;

    BungeeCommandManager() {}

    @Override
    public @NotNull Command register(@NotNull CommandInfo commandInfo, @NotNull Command commandExecutor) {
        pluginInstance.getProxy().getPluginManager().registerCommand(pluginInstance, commandExecutor);
        registeredCommands.put(commandInfo.name(), commandExecutor);
        return commandExecutor;
    }

    @Override
    public Command register(CommandTree commandTree) {
        CommandInfo commandInfo = commandTree.commandInfo;
        BungeeCommand commandExecutor = new BungeeCommand(commandTree);
        return register(commandInfo, commandExecutor);
    }

    /**
     * 注销一个命令
     * @param commandName 命令的名字
     * @return 被注销的命令，若不存在则返回 Optional.empty()
     */
    @Override
    public Optional<Command> unregister(String commandName) {
        Command command = registeredCommands.get(commandName);
        if (command == null)
            return Optional.empty();
        ProxyServer.getInstance().getPluginManager().unregisterCommand(command);
        registeredCommands.remove(commandName);
        return Optional.of(command);
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

    @Override
    public void lifecycle(CrypticLibPlugin plugin, Lifecycle lifeCycle) {
        this.pluginInstance = (Plugin) plugin;
    }

}
