package crypticlib;

import crypticlib.annotation.AnnotationProcessor;
import crypticlib.chat.LangConfigContainer;
import crypticlib.chat.LangConfigHandler;
import crypticlib.chat.MessageSender;
import crypticlib.command.BukkitCommand;
import crypticlib.command.CommandInfo;
import crypticlib.command.CommandManager;
import crypticlib.config.ConfigContainer;
import crypticlib.config.ConfigHandler;
import crypticlib.config.ConfigWrapper;
import crypticlib.listener.BukkitListener;
import crypticlib.perm.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BukkitPlugin extends JavaPlugin {

    protected final Map<String, ConfigContainer> configContainerMap = new ConcurrentHashMap<>();
    protected final Map<String, LangConfigContainer> langConfigContainerMap = new ConcurrentHashMap<>();
    private final String defaultConfigFileName = "config.yml";
    private Integer lowestSupportVersion = 11200;
    private Integer highestSupportVersion = 12004;

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
                    boolean reg = true;
                    try {
                        clazz.getDeclaredMethods();
                    } catch (NoClassDefFoundError ignored) {
                        reg = false;
                    }

                    if (!reg)
                        return;
                    Listener listener = (Listener) annotationProcessor.getClassInstance(clazz);
                    Bukkit.getPluginManager().registerEvents(listener, this);
                })
            .regClassAnnotationProcessor(
                BukkitCommand.class,
                (annotation, clazz) -> {
                    TabExecutor tabExecutor = (TabExecutor) annotationProcessor.getClassInstance(clazz);
                    BukkitCommand bukkitCommand = (BukkitCommand) annotation;
                    CrypticLib.commandManager().register(this, new CommandInfo(bukkitCommand), tabExecutor);
                })
            .regClassAnnotationProcessor(
                ConfigHandler.class,
                (annotation, clazz) -> {
                    ConfigHandler configHandler = (ConfigHandler) annotation;
                    String path = configHandler.path();
                    if (!path.endsWith(".yml") && !path.endsWith(".yaml"))
                        path += ".yml";
                    ConfigWrapper configWrapper = new ConfigWrapper(this, path);
                    ConfigContainer configContainer = new ConfigContainer(clazz, configWrapper);
                    configContainerMap.put(path, configContainer);
                    configContainer.reload();
                }, AnnotationProcessor.ProcessPriority.LOWEST)
            .regClassAnnotationProcessor(
                LangConfigHandler.class,
                (annotation, clazz) -> {
                    LangConfigHandler langConfigHandler = (LangConfigHandler) annotation;
                    String langFileFolder = langConfigHandler.langFileFolder();
                    String defLang = langConfigHandler.defLang();
                    LangConfigContainer langConfigContainer = new LangConfigContainer(this, clazz, langFileFolder, defLang);
                    langConfigContainerMap.put(langFileFolder, langConfigContainer);
                    langConfigContainer.reload();
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
        ConfigContainer defConfig = new ConfigContainer(this.getClass(), new ConfigWrapper(this, defaultConfigFileName));
        defConfig.reload();
        configContainerMap.put(defaultConfigFileName, defConfig);
    }

    @Override
    public void reloadConfig() {
        configContainerMap.forEach((path, container) -> container.reload());
        langConfigContainerMap.forEach((langFolder, container) -> container.reload());
    }

    private void checkVersion() {
        int version = CrypticLib.minecraftVersion();
        if (version > highestSupportVersion || version < lowestSupportVersion) {
            MessageSender.info(this.getName() + " &c&lUnsupported Version");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public Integer lowestSupportVersion() {
        return lowestSupportVersion;
    }

    public void setLowestSupportVersion(Integer lowestSupportVersion) {
        this.lowestSupportVersion = lowestSupportVersion;
    }

    public Integer highestSupportVersion() {
        return highestSupportVersion;
    }

    public void setHighestSupportVersion(Integer highestSupportVersion) {
        this.highestSupportVersion = highestSupportVersion;
    }

}
