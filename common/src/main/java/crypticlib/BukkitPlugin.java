package crypticlib;

import crypticlib.annotations.BukkitCommand;
import crypticlib.annotations.BukkitListener;
import crypticlib.command.IPluginCmdExecutor;
import crypticlib.util.MsgUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class BukkitPlugin extends JavaPlugin {

    private static BukkitPlugin INSTANCE;
    private static int lowestSupportVersion = 11300;
    private static int highestSupportVersion = 12002;

    protected BukkitPlugin() {
        super();
        INSTANCE = this;
    }

    @Override
    public final void onEnable() {
        loadListenersAndCommands();
        enable();
        checkVersion();
    }

    @Override
    public final void onDisable() {
        CrypticLib.platform().scheduler().cancelTasks(this);
        disable();
    }

    /**
     * 插件开始加载执行的方法
     */
    public void enable() {}

    /**
     * 插件卸载时执行的方法
     */
    public void disable() {}

    public static BukkitPlugin instance() {
        return INSTANCE;
    }

    private static void checkVersion() {
        int version = CrypticLib.minecraftVersion();
        if (version > highestSupportVersion || version < lowestSupportVersion) {
            MsgUtil.info("&c&lUnsupported Version");
            Bukkit.getPluginManager().disablePlugin(BukkitPlugin.INSTANCE);
        }
    }

    public static int lowestSupportVersion() {
        return lowestSupportVersion;
    }

    public static void setLowestSupportVersion(int lowestSupportVersion) {
        BukkitPlugin.lowestSupportVersion = lowestSupportVersion;
    }

    public static int highestSupportVersion() {
        return highestSupportVersion;
    }

    public static void setHighestSupportVersion(int highestSupportVersion) {
        BukkitPlugin.highestSupportVersion = highestSupportVersion;
    }

    private void loadListenersAndCommands() {
        Set<Class<?>> listenerClasses = new HashSet<>();
        Set<Class<?>> pluginCommandClasses = new HashSet<>();
        //扫描类
        Enumeration<JarEntry> entries;
        try {
            JarFile pluginJar = new JarFile(getFile());
            entries = pluginJar.entries();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (entries.hasMoreElements()) {
            try {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
                    Class<?> clazz = getClassLoader().loadClass(className);
                    if (clazz.isAnnotationPresent(BukkitCommand.class)) {
                        pluginCommandClasses.add(clazz);
                    }
                    if (clazz.isAnnotationPresent(BukkitListener.class)) {
                        listenerClasses.add(clazz);
                    }
                }
            } catch (ClassNotFoundException | NoClassDefFoundError ignored) {}
        }

        //注册监听器
        for (Class<?> listenerClass : listenerClasses) {
            try {
                if (listenerClass.isEnum()) {
                    for (Object listenerEnum : listenerClass.getEnumConstants()) {
                        Bukkit.getPluginManager().registerEvents((Listener) listenerEnum, this);
                    }
                } else {
                    Constructor<?> listenerConstructor = listenerClass.getDeclaredConstructor();
                    listenerConstructor.setAccessible(true);
                    Listener listener = (Listener) listenerConstructor.newInstance();
                    Bukkit.getPluginManager().registerEvents(listener, this);
                }
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        //注册命令
        Method getCommandMapMethod;
        CommandMap commandMap;
        try {
            getCommandMapMethod = Bukkit.getServer().getClass().getMethod("getCommandMap");
            commandMap = (CommandMap) getCommandMapMethod.invoke(Bukkit.getServer());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (Class<?> commandClass : pluginCommandClasses) {
            try {
                BukkitCommand commandAnnotation = commandClass.getAnnotation(BukkitCommand.class);
                if (commandClass.isEnum()) {
                    for (Object cmdExecutorEnum : commandClass.getEnumConstants()) {
                        regCommand(commandMap, (IPluginCmdExecutor) cmdExecutorEnum, commandAnnotation);
                    }
                } else {
                    Constructor<?> commandConstructor = commandClass.getDeclaredConstructor();
                    commandConstructor.setAccessible(true);
                    IPluginCmdExecutor cmdExecutor = (IPluginCmdExecutor) commandConstructor.newInstance();
                    regCommand(commandMap, cmdExecutor, commandAnnotation);
                }
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void regCommand(CommandMap commandMap, IPluginCmdExecutor pluginCommand, BukkitCommand commandAnnotation) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        PluginCommand command;
        Constructor<PluginCommand> pluginCommandConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        pluginCommandConstructor.setAccessible(true);
        command = pluginCommandConstructor.newInstance(commandAnnotation.name(), this);
        command.setAliases(new ArrayList<>(Arrays.asList(commandAnnotation.alias())));
        command.setExecutor(pluginCommand);
        command.setTabCompleter(pluginCommand);
        if (!commandAnnotation.description().isEmpty())
            command.setDescription(commandAnnotation.description());
        if (!commandAnnotation.permission().isEmpty())
            command.setPermission(commandAnnotation.permission());
        if (!commandAnnotation.usage().isEmpty())
            command.setUsage(commandAnnotation.usage());

        commandMap.register(this.getName().toLowerCase(), command);
    }

}
