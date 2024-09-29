package crypticlib.command;

import com.velocitypowered.api.command.Command;
import crypticlib.VelocityPlugin;
import crypticlib.lifecycle.AutoTask;
import crypticlib.lifecycle.LifeCycle;
import crypticlib.lifecycle.TaskRule;
import crypticlib.lifecycle.VelocityLifeCycleTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AutoTask(
    rules = {
        @TaskRule(lifeCycle = LifeCycle.INIT)
    }
)
public enum VelocityCommandManager implements VelocityLifeCycleTask, CommandManager<VelocityPlugin, Command, Command> {

    INSTANCE;

    private VelocityPlugin plugin;
    private final Map<String, Command> registeredCommands = new ConcurrentHashMap<>();

    @Override
    public Command register(@NotNull VelocityPlugin plugin, @NotNull CommandInfo commandInfo, @NotNull Command command) {
        com.velocitypowered.api.command.CommandManager commandManager = plugin.proxyServer().getCommandManager();
        commandManager.register(
            commandManager.metaBuilder(commandInfo.name())
                .aliases(commandInfo.aliases().toArray(new String[]{}))
                .plugin(plugin)
                .build(),
            command
        );
        registeredCommands.put(commandInfo.name(), command);
        return command;
    }

    @Override
    public @Nullable Command unregister(String commandName) {
        com.velocitypowered.api.command.CommandManager commandManager = plugin.proxyServer().getCommandManager();
        commandManager.unregister(commandName);
        return registeredCommands.remove(commandName);
    }

    @Override
    public void unregisterAll() {
        registeredCommands.forEach((name, command) -> unregister(name));
        registeredCommands.clear();
    }

    @Override
    public Map<String, Command> registeredCommands() {
        return registeredCommands;
    }

    @Override
    public void run(VelocityPlugin plugin, LifeCycle lifeCycle) {
        this.plugin = plugin;
    }

}
