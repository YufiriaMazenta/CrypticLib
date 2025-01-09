package crypticlib.command;

import crypticlib.perm.PermInfo;
import crypticlib.util.ReflectionHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitCommandManager implements CommandManager<Plugin, TabExecutor, Command> {

    public static final BukkitCommandManager INSTANCE = new BukkitCommandManager();

    private final CommandMap serverCommandMap;
    private final Constructor<?> pluginCommandConstructor;
    private final Map<String, Command> registeredCommands = new ConcurrentHashMap<>();

    BukkitCommandManager() {
        Method getCommandMapMethod = ReflectionHelper.getMethod(Bukkit.getServer().getClass(), "getCommandMap");
        serverCommandMap = (CommandMap) ReflectionHelper.invokeMethod(getCommandMapMethod, Bukkit.getServer());
        pluginCommandConstructor = ReflectionHelper.getDeclaredConstructor(PluginCommand.class, String.class, Plugin.class);
    }

    @Override
    public Command register(@NotNull Plugin plugin, @NotNull CommandInfo commandInfo, @NotNull TabExecutor commandExecutor) {
        PluginCommand pluginCommand = (PluginCommand) ReflectionHelper.invokeDeclaredConstructor(pluginCommandConstructor, commandInfo.name(), plugin);
        pluginCommand.setAliases(commandInfo.aliases());
        String description = commandInfo.description();
        pluginCommand.setDescription(description == null ? "" : description);
        PermInfo permInfo = commandInfo.permission();
        if (permInfo != null)
            pluginCommand.setPermission(permInfo.permission());
        String usage = commandInfo.usage();
        pluginCommand.setUsage(usage == null ? "" : usage)  ;
        pluginCommand.setExecutor(commandExecutor);
        pluginCommand.setTabCompleter(commandExecutor);
        serverCommandMap.register(plugin.getName(), pluginCommand);
        registeredCommands.put(commandInfo.name(), pluginCommand);
        return pluginCommand;
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
        serverCommandMap.getKnownCommands().remove(commandName);
        command.unregister(serverCommandMap);
        registeredCommands.remove(commandName);
        return command;
    }

    /**
     * 注销所有通过CommandManager的命令
     */
    @Override
    public void unregisterAll() {
        registeredCommands.forEach(
            (pluginName, command) -> {
                command.unregister(serverCommandMap);
            }
        );
        registeredCommands.clear();
    }

    @Override
    public Map<String, Command> registeredCommands() {
        return registeredCommands;
    }

}
