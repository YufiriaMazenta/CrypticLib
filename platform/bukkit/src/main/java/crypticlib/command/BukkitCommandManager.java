package crypticlib.command;

import crypticlib.perm.PermInfo;
import crypticlib.util.ReflectionHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum BukkitCommandManager implements CommandManager<Plugin, TabExecutor, PluginCommand> {

    INSTANCE;

    private final CommandMap serverCommandMap;
    private final Map<String, Command> serverCommandMapKnownCommands;
    private final Constructor<?> pluginCommandConstructor;
    private final Map<String, PluginCommand> registeredCommands = new ConcurrentHashMap<>();
    private final Method serverSyncCommandsMethod;

    BukkitCommandManager() {
        Method getCommandMapMethod = ReflectionHelper.getMethod(Bukkit.getServer().getClass(), "getCommandMap");
        serverCommandMap = (CommandMap) ReflectionHelper.invokeMethod(getCommandMapMethod, Bukkit.getServer());
        Field knownCommandsField = ReflectionHelper.getDeclaredField(SimpleCommandMap.class, "knownCommands");
        serverCommandMapKnownCommands = ReflectionHelper.getDeclaredFieldObj(knownCommandsField, serverCommandMap);
        pluginCommandConstructor = ReflectionHelper.getDeclaredConstructor(PluginCommand.class, String.class, Plugin.class);
        serverSyncCommandsMethod = ReflectionHelper.getMethod(Bukkit.getServer().getClass(), "syncCommands");
    }

    @Override
    public PluginCommand register(@NotNull Plugin plugin, @NotNull CommandInfo commandInfo, @NotNull TabExecutor commandExecutor) {
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
    public PluginCommand unregister(String commandName) {
        PluginCommand command = registeredCommands.get(commandName);
        if (command == null)
            return null;
        command.unregister(serverCommandMap);

        //先移除不带命名空间的
        serverCommandMapKnownCommands.remove(commandName);
        for (String alias : command.getAliases()) {
            serverCommandMapKnownCommands.remove(alias);
        }

        //再移除带命名空间的
        String commandNamespace = command.getPlugin().getName().toLowerCase(Locale.ENGLISH);
        serverCommandMapKnownCommands.remove(commandNamespace + ":" + commandName.toLowerCase(Locale.ENGLISH));
        for (String alias : command.getAliases()) {
            serverCommandMapKnownCommands.remove(commandNamespace + ":" + alias);
        }

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
    public Map<String, PluginCommand> registeredCommands() {
        return registeredCommands;
    }

    /**
     * 同步命令,会刷新控制台与玩家的命令列表,一般在动态注册/卸载命令后调用
     */
    public void syncCommands() {
        ReflectionHelper.invokeMethod(serverSyncCommandsMethod, Bukkit.getServer());
    }

}
