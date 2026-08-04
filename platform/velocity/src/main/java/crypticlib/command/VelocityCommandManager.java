package crypticlib.command;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandMeta;
import crypticlib.CrypticLibPlugin;
import crypticlib.PlatformSide;
import crypticlib.VelocityPlugin;
import crypticlib.lifecycle.LifeCycleTaskSettings;
import crypticlib.lifecycle.LifeCycle;
import crypticlib.lifecycle.LifeCycleTask;
import crypticlib.lifecycle.TaskRule;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@LifeCycleTaskSettings(
    rules = {
        @TaskRule(lifeCycle = LifeCycle.INIT)
    },
    platforms = PlatformSide.VELOCITY
)
public enum VelocityCommandManager implements LifeCycleTask, CommandManager<Command, Command> {

    INSTANCE;

    private VelocityPlugin plugin;
    private final Map<String, Command> registeredCommands = new ConcurrentHashMap<>();
    private final Map<String, CommandMeta> registeredMetas = new ConcurrentHashMap<>();

    @Override
    public Command register(@NotNull CommandInfo commandInfo, @NotNull Command command) {
        com.velocitypowered.api.command.CommandManager commandManager = plugin.proxyServer().getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder(commandInfo.name())
            .aliases(commandInfo.aliases().toArray(new String[]{}))
            .plugin(plugin)
            .build();
        commandManager.register(commandMeta, command);
        registeredCommands.put(commandInfo.name(), command);
        registeredMetas.put(commandInfo.name(), commandMeta);
        return command;
    }

    @Override
    public Command register(CommandTree commandTree) {
        CommandInfo commandInfo = commandTree.commandInfo();
        Command command = new VelocityCommand(commandTree);
        return register(commandInfo, command);
    }

    @Override
    public Optional<Command> unregister(String commandName) {
        //先确认是本管理器注册的命令,避免误注销其它插件的同名命令
        Command command = registeredCommands.remove(commandName);
        if (command == null)
            return Optional.empty();
        com.velocitypowered.api.command.CommandManager commandManager = plugin.proxyServer().getCommandManager();
        CommandMeta commandMeta = registeredMetas.remove(commandName);
        if (commandMeta != null) {
            //通过 CommandMeta 一次性移除主名及所有别名
            commandManager.unregister(commandMeta);
        } else {
            commandManager.unregister(commandName);
        }
        return Optional.of(command);
    }

    @Override
    public void unregisterAll() {
        new ArrayList<>(registeredCommands.keySet()).forEach(this::unregister);
        registeredCommands.clear();
        registeredMetas.clear();
    }

    @Override
    public Map<String, Command> registeredCommands() {
        return registeredCommands;
    }

    @Override
    public void lifecycle(CrypticLibPlugin plugin, LifeCycle lifeCycle) {
        this.plugin = (VelocityPlugin) plugin;
    }

}
