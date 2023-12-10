package crypticlib;

import crypticlib.annotation.AnnotationProcessor;
import crypticlib.command.BukkitCommand;
import crypticlib.command.CommandInfo;
import crypticlib.command.CommandManager;
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
    private int highestSupportVersion = 12004;

    protected BukkitPlugin() {
        super();
    }

    @Override
    public final void onLoad() {
        AnnotationProcessor annotationProcessor = AnnotationProcessor.INSTANCE;
        annotationProcessor
            .regClassAnnotationProcessor(
                BukkitListener.class,
                (annotation, clazz) -> {
                    Listener listener = (Listener) annotationProcessor.getClassInstance(clazz);
                    Bukkit.getPluginManager().registerEvents(listener, this);
                })
            .regClassAnnotationProcessor(
                BukkitCommand.class,
                (annotation, clazz) -> {
                    TabExecutor tabExecutor = (TabExecutor) annotationProcessor.getClassInstance(clazz);
                    BukkitCommand bukkitCommand = (BukkitCommand) annotation;
                    CommandManager.INSTANCE.register(this, new CommandInfo(bukkitCommand), tabExecutor);
                })
            .regClassAnnotationProcessor(
                YamlConfigHandler.class,
                (annotation, clazz) -> {
                    YamlConfigHandler yamlConfigHandler = (YamlConfigHandler) annotation;
                    String path = yamlConfigHandler.path();
                    if (!path.endsWith(".yml") && !path.endsWith(".yaml"))
                        path += ".yml";
                    YamlConfigWrapper configWrapper = new YamlConfigWrapper(this, path);
                    YamlConfigContainer configContainer = new YamlConfigContainer(clazz, configWrapper);
                    configContainerMap.put(path, configContainer);
                    configContainer.reload();
                }, AnnotationProcessor.ProcessPriority.LOWEST);
        load();
    }

    @Override
    public final void onEnable() {
        AnnotationProcessor.INSTANCE.scanJar(getFile());
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
     * 插件开始加载时执行的方法
     */
    public void load() {

    }

    /**
     * 插件开始启用时执行的方法
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
            MsgUtil.info(this.getName() + " &c&lUnsupported Version");
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

}
