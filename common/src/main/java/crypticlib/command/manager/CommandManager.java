package crypticlib.command.manager;

import crypticlib.command.CommandInfo;
import crypticlib.command.SubcommandHandler;
import crypticlib.perm.PermInfo;
import crypticlib.util.ReflectUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum CommandManager {

    INSTANCE;

    private final CommandMap serverCommandMap;
    private final Constructor<?> pluginCommandConstructor;
    private final Map<String, Map<String, Command>> registeredCommands = new ConcurrentHashMap<>();

    CommandManager() {
        Method getCommandMapMethod = ReflectUtil.getMethod(Bukkit.getServer().getClass(), "getCommandMap");
        serverCommandMap = (CommandMap) ReflectUtil.invokeMethod(getCommandMapMethod, Bukkit.getServer());
        pluginCommandConstructor = ReflectUtil.getDeclaredConstructor(PluginCommand.class, String.class, Plugin.class);
    }

    public Command register(@NotNull Plugin plugin, @NotNull CommandInfo commandInfo, @NotNull TabExecutor commandExecutor) {
        PluginCommand pluginCommand = (PluginCommand) ReflectUtil.invokeDeclaredConstructor(pluginCommandConstructor, commandInfo.name(), plugin);
        pluginCommand.setAliases(Arrays.asList(commandInfo.aliases()));
        pluginCommand.setDescription(commandInfo.description());
        PermInfo permInfo = commandInfo.permission();
        if (permInfo != null)
            pluginCommand.setPermission(permInfo.permission());
        pluginCommand.setUsage(commandInfo.usage());
        pluginCommand.setExecutor(commandExecutor);
        pluginCommand.setTabCompleter(commandExecutor);
        String pluginName = plugin.getName();
        serverCommandMap.register(pluginName, pluginCommand);
        if (registeredCommands.containsKey(pluginName)) {
            Map<String, Command> commandMap = registeredCommands.get(pluginName);
            commandMap.put(commandInfo.name(), pluginCommand);
        } else {
            Map<String, Command> commandMap = new ConcurrentHashMap<>();
            commandMap.put(commandInfo.name(), pluginCommand);
            registeredCommands.put(pluginName, commandMap);
        }
        return pluginCommand;
    }

    /**
     * 注销一个命令
     * @param plugin 命令所属的插件
     * @param commandName 命令的名字
     * @return 被注销的命令，若为null即不存在此命令
     */
    @Nullable
    public Command unregister(Plugin plugin, String commandName) {
        String pluginName = plugin.getName();
        if (!registeredCommands.containsKey(pluginName))
            return null;
        Map<String, Command> commandMap = registeredCommands.get(commandName);
        Command command = commandMap.get(pluginName);
        if (command == null)
            return null;
        command.unregister(serverCommandMap);
        commandMap.remove(commandName);
        if (commandMap.isEmpty()) {
            registeredCommands.remove(pluginName);
        }
        return command;
    }

    /**
     * 注销所有通过CommandManager的命令
     */
    public void unregisterAll() {
        registeredCommands.forEach(
            (plugin, commandMap) -> {
                for (Command command : commandMap.values()) {
                    command.unregister(serverCommandMap);
                }
            }
        );
        registeredCommands.clear();
    }

    public Map<String, Map<String, Command>> registeredCommands() {
        return registeredCommands;
    }

    public static SubcommandHandler subcommand(@NotNull String name) {
        return new SubcommandHandler(name);
    }

}
