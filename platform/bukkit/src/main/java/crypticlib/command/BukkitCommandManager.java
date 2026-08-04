package crypticlib.command;

import crypticlib.CrypticLibPlugin;
import crypticlib.PlatformSide;
import crypticlib.lifecycle.LifecycleTaskSettings;
import crypticlib.lifecycle.LifecycleTask;
import crypticlib.lifecycle.Lifecycle;
import crypticlib.lifecycle.LifecycleRule;
import crypticlib.perm.PermInfo;
import crypticlib.util.ReflectionHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@LifecycleTaskSettings(rules = @LifecycleRule(lifeCycle = Lifecycle.INIT), platforms = PlatformSide.BUKKIT)
public enum BukkitCommandManager implements CommandManager<TabExecutor, PluginCommand>, LifecycleTask {

    INSTANCE;

    private Plugin pluginInstance;
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
    public PluginCommand register(@NotNull CommandInfo commandInfo, @NotNull TabExecutor commandExecutor) {
        PluginCommand pluginCommand = (PluginCommand) ReflectionHelper.invokeDeclaredConstructor(pluginCommandConstructor, commandInfo.name(), pluginInstance);
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
        serverCommandMap.register(pluginInstance.getName(), pluginCommand);
        registeredCommands.put(commandInfo.name(), pluginCommand);
        return pluginCommand;
    }

    @Override
    public PluginCommand register(CommandTree commandTree) {
        CommandInfo commandInfo = commandTree.commandInfo();
        TabExecutor commandExecutor = new BukkitCommand(commandTree);
        return register(commandInfo, commandExecutor);
    }

    /**
     * 注销一个命令
     * @param commandName 命令的名字
     * @return 被注销的命令，若不存在则返回 Optional.empty()
     */
    @Override
    public Optional<PluginCommand> unregister(String commandName) {
        PluginCommand command = registeredCommands.get(commandName);
        if (command == null)
            return Optional.empty();
        command.unregister(serverCommandMap);

        //SimpleCommandMap 存入 knownCommands 时会将主名与别名统一转为小写,移除时也需使用小写键
        String lowerCommandName = commandName.toLowerCase(Locale.ENGLISH);

        //先移除不带命名空间的
        serverCommandMapKnownCommands.remove(lowerCommandName);
        for (String alias : command.getAliases()) {
            serverCommandMapKnownCommands.remove(alias.toLowerCase(Locale.ENGLISH));
        }

        //再移除带命名空间的
        String commandNamespace = command.getPlugin().getName().toLowerCase(Locale.ENGLISH);
        serverCommandMapKnownCommands.remove(commandNamespace + ":" + lowerCommandName);
        for (String alias : command.getAliases()) {
            serverCommandMapKnownCommands.remove(commandNamespace + ":" + alias.toLowerCase(Locale.ENGLISH));
        }

        registeredCommands.remove(commandName);
        return Optional.of(command);
    }

    /**
     * 注销所有通过CommandManager的命令
     */
    @Override
    public void unregisterAll() {
        //复用 unregister 的完整清理逻辑,确保 knownCommands 中的主名、别名及带命名空间的键全部被移除
        new ArrayList<>(registeredCommands.keySet()).forEach(this::unregister);
        registeredCommands.clear();
        //刷新控制台与玩家的命令列表,移除残留的失效命令
        syncCommands();
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

    @Override
    public void lifecycle(CrypticLibPlugin plugin, Lifecycle lifeCycle) {
        this.pluginInstance = (Plugin) plugin;
    }

}
