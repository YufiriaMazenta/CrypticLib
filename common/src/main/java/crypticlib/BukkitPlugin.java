package crypticlib;

import crypticlib.command.BukkitCommand;
import crypticlib.command.CommandInfo;
import crypticlib.command.impl.RootCmdExecutor;
import crypticlib.config.yaml.YamlConfigContainer;
import crypticlib.config.yaml.YamlConfigHandler;
import crypticlib.config.yaml.YamlConfigWrapper;
import crypticlib.listener.BukkitListener;
import crypticlib.util.MsgUtil;
import crypticlib.util.ReflectUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class BukkitPlugin extends JavaPlugin {

    private final Map<String, YamlConfigContainer> configContainerMap = new ConcurrentHashMap<>();
    private final String defaultConfigFileName = "config.yml";
    private int lowestSupportVersion = 11200;
    private int highestSupportVersion = 12002;

    protected BukkitPlugin() {
        super();
    }

    @Override
    public final void onEnable() {
        scanClasses();
        enable();
        checkVersion();
    }

    @Override
    public final void onDisable() {
        disable();
        CrypticLib.platform().scheduler().cancelTasks(this);
        CrypticLib.commandManager().unregisterAll();
    }

    /**
     * 插件开始加载执行的方法
     */
    public void enable() {
    }

    /**
     * 插件卸载时执行的方法
     */
    public void disable() {
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        if (configContainerMap.containsKey(defaultConfigFileName)) {
            return configContainerMap.get(defaultConfigFileName).configWrapper().config();
        }
        throw new UnsupportedOperationException("No default config file");
    }

    @Override
    public void saveConfig() {
        configContainerMap.forEach((path, configContainer) -> configContainer.configWrapper().saveConfig());
    }

    @Override
    public void saveDefaultConfig() {
        YamlConfigContainer defConfig = new YamlConfigContainer(this.getClass(), new YamlConfigWrapper(this, defaultConfigFileName));
        defConfig.reload();
        configContainerMap.put(defaultConfigFileName, defConfig);
    }

    @Override
    public void reloadConfig() {
        configContainerMap.forEach((path, container) -> container.reload());
    }

    private void checkVersion() {
        int version = CrypticLib.minecraftVersion();
        if (version > highestSupportVersion || version < lowestSupportVersion) {
            MsgUtil.info("&c&lUnsupported Version");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public int lowestSupportVersion() {
        return lowestSupportVersion;
    }

    public void setLowestSupportVersion(int lowestSupportVersion) {
        this.lowestSupportVersion = lowestSupportVersion;
    }

    public int highestSupportVersion() {
        return highestSupportVersion;
    }

    public void setHighestSupportVersion(int highestSupportVersion) {
        this.highestSupportVersion = highestSupportVersion;
    }

    private void scanClasses() {
        Set<Class<?>> listenerClasses = new HashSet<>();
        Set<Class<?>> pluginCommandClasses = new HashSet<>();
        Set<Class<?>> configContainerClasses = new HashSet<>();
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
                    if (clazz.isAnnotationPresent(YamlConfigHandler.class)) {
                        configContainerClasses.add(clazz);
                    }
                }
            } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
            }
        }

        regConfigs(configContainerClasses);
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
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
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
                TabExecutor cmdExecutor = (TabExecutor) ReflectUtil.invokeDeclaredConstructor(commandConstructor);
                CrypticLib.commandManager().register(this, new CommandInfo(commandAnnotation), cmdExecutor);
            }
        }
    }

    private void regConfigs(Set<Class<?>> configContainerClasses) {
        for (Class<?> configContainerClass : configContainerClasses) {
            YamlConfigHandler yamlConfigHandlerAnnotation = configContainerClass.getAnnotation(YamlConfigHandler.class);
            String path = yamlConfigHandlerAnnotation.path();
            if (!path.endsWith(".yml") && !path.endsWith(".yaml"))
                path += ".yml";
            YamlConfigWrapper configWrapper = new YamlConfigWrapper(this, path);
            YamlConfigContainer configContainer = new YamlConfigContainer(configContainerClass, configWrapper);
            configContainerMap.put(path, configContainer);
            configContainer.reload();
        }
    }

}
