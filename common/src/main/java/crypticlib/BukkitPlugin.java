package crypticlib;

import crypticlib.command.api.BukkitCommand;
import crypticlib.command.api.CommandInfo;
import crypticlib.command.impl.RootCmdExecutor;
import crypticlib.listener.BukkitListener;
import crypticlib.util.MsgUtil;
import crypticlib.util.ReflectUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class BukkitPlugin extends JavaPlugin {

    private static BukkitPlugin INSTANCE;
    private static int lowestSupportVersion = 11200;
    private static int highestSupportVersion = 12002;

    protected BukkitPlugin() {
        super();
        INSTANCE = this;
    }

    @Override
    public final void onEnable() {
        enable();
        scanListenersAndCommands();
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

    private void scanListenersAndCommands() {
        Set<Class<?>> listenerClasses = new HashSet<>();
        Set<Class<?>> pluginCommandClasses = new HashSet<>();
        //扫描类
        Enumeration<JarEntry> entries;
        JarFile pluginJar;
        try {
            pluginJar = new JarFile(getFile());
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
        regListeners(listenerClasses);
        //注册命令
        regCommands(pluginCommandClasses);

        try {
            pluginJar.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void regListeners(Set<Class<?>> listenerClasses) {
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
    }


    private void regCommands(Set<Class<?>> pluginCommandClasses) {
        for (Class<?> commandClass : pluginCommandClasses) {
            BukkitCommand commandAnnotation = commandClass.getAnnotation(BukkitCommand.class);
            if (commandClass.isEnum()) {
                for (Object cmdExecutorEnum : commandClass.getEnumConstants()) {
                    CrypticLib.commandManager().register(this, new CommandInfo(commandAnnotation), (RootCmdExecutor) cmdExecutorEnum);
                }
            } else {
                Constructor<?> commandConstructor = ReflectUtil.getDeclaredConstructor(commandClass);
                RootCmdExecutor cmdExecutor = (RootCmdExecutor) ReflectUtil.invokeDeclaredConstructor(commandConstructor);
                CrypticLib.commandManager().register(this, new CommandInfo(commandAnnotation), cmdExecutor);
            }
        }
    }

}
