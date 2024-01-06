package crypticlib.command;

import crypticlib.CrypticLib;
import crypticlib.perm.PermissionManager;
import crypticlib.util.ReflectUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public enum CommandManager {

    INSTANCE;

    private final CommandMap serverCommandMap;
    private final Constructor<?> pluginCommandConstructor;
    private final List<Command> registeredCommands;

    CommandManager() {
        Method getCommandMapMethod = ReflectUtil.getMethod(Bukkit.getServer().getClass(), "getCommandMap");
        serverCommandMap = (CommandMap) ReflectUtil.invokeMethod(getCommandMapMethod, Bukkit.getServer());
        pluginCommandConstructor = ReflectUtil.getDeclaredConstructor(PluginCommand.class, String.class, Plugin.class);
        registeredCommands = new CopyOnWriteArrayList<>();
    }

    public static SubcmdExecutor subcommand(@NotNull String name) {
        return new SubcmdExecutor(name);
    }

    public CommandManager register(@NotNull Plugin plugin, @NotNull CommandInfo commandInfo, @NotNull TabExecutor commandExecutor) {
        PluginCommand pluginCommand = (PluginCommand) ReflectUtil.invokeDeclaredConstructor(pluginCommandConstructor, commandInfo.name(), plugin);
        pluginCommand.setAliases(Arrays.asList(commandInfo.aliases()));
        pluginCommand.setDescription(commandInfo.description());
        pluginCommand.setPermission(commandInfo.permission());
        pluginCommand.setUsage(commandInfo.usage());
        pluginCommand.setExecutor(commandExecutor);
        pluginCommand.setTabCompleter(commandExecutor);
        serverCommandMap.register(plugin.getName(), pluginCommand);
        registeredCommands.add(pluginCommand);
        CrypticLib.permissionManager().regPerm(commandInfo.permission(), commandInfo.permDef());
        return this;
    }

    public CommandManager unregisterAll() {
        for (Command registeredCommand : registeredCommands) {
            registeredCommand.unregister(serverCommandMap);
        }
        registeredCommands.clear();
        return this;
    }

}
