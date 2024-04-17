package crypticlib;

import crypticlib.annotation.AnnotationProcessor;
import crypticlib.chat.MsgSender;
import crypticlib.command.CommandHandler;
import crypticlib.command.SubcommandHandler;
import crypticlib.command.annotation.Command;
import crypticlib.command.annotation.Subcommand;
import crypticlib.command.manager.CommandManager;
import crypticlib.config.ConfigContainer;
import crypticlib.config.ConfigHandler;
import crypticlib.config.ConfigWrapper;
import crypticlib.lang.LangEntryContainer;
import crypticlib.lang.LangHandler;
import crypticlib.lang.LangManager;
import crypticlib.listener.BukkitListener;
import crypticlib.util.ReflectUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class BukkitPlugin extends JavaPlugin {

    protected final Map<String, ConfigContainer> configContainerMap = new ConcurrentHashMap<>();
    protected final List<Disabler> disablerList = new CopyOnWriteArrayList<>();
    protected final List<Reloader> reloaderList = new CopyOnWriteArrayList<>();
    protected final String defaultConfigFileName = "config.yml";

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
                    if (!Listener.class.isAssignableFrom(clazz))
                        return;
                    try {
                        Listener listener = (Listener) annotationProcessor.getClassInstance(clazz);
                        Bukkit.getPluginManager().registerEvents(listener, this);
                    } catch (NoClassDefFoundError ignored) {}
                })
            .regClassAnnotationProcessor(
                Command.class,
                (annotation, clazz) -> {
                    if (!CommandHandler.class.isAssignableFrom(clazz)) {
                        MsgSender.info("&e@Command annotation is used on non-CommandHandler implementation class:" + clazz.getName());
                        return;
                    }
                    CommandHandler commandHandler = (CommandHandler) annotationProcessor.getClassInstance(clazz);
                    for (Field field : commandHandler.getClass().getDeclaredFields()) {
                        if (!field.isAnnotationPresent(Subcommand.class))
                            continue;
                        if (SubcommandHandler.class.isAssignableFrom(field.getType())) {
                            SubcommandHandler subcommand = ReflectUtil.getDeclaredFieldObj(field, commandHandler);
                            commandHandler.regSub(subcommand);
                        }
                    }
                    commandHandler.register(this);
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
                LangHandler.class,
                (annotation, clazz) -> {
                    LangHandler langHandler = (LangHandler) annotation;
                    String langFileFolder = langHandler.langFileFolder();
                    String defLang = langHandler.defLang();
                    LangEntryContainer langEntryContainer = new LangEntryContainer(this, clazz, langFileFolder, defLang);
                    LangManager.INSTANCE.loadLangEntryContainer(langFileFolder, langEntryContainer);
                }, AnnotationProcessor.ProcessPriority.LOWEST)
            .regClassAnnotationProcessor(
                OnDisable.class,
                (annotation, clazz) -> {
                    if (!Disabler.class.isAssignableFrom(clazz))
                        return;
                    Disabler disabler = (Disabler) annotationProcessor.getClassInstance(clazz);
                    disablerList.add(disabler);
                }
            )
            .regClassAnnotationProcessor(
                OnReload.class,
                (annotation, clazz) -> {
                    if (!Reloader.class.isAssignableFrom(clazz))
                        return;
                    Reloader reloader = (Reloader) annotationProcessor.getClassInstance(clazz);
                    reloaderList.add(reloader);
                }
            );
        load();
    }

    @Override
    public final void onEnable() {
        AnnotationProcessor.INSTANCE.scanJar(getFile());
        enable();
    }

    @Override
    public final void onDisable() {
        disable();
        reloaderList.clear();
        disablerList.forEach(it -> {
            try {
                it.disable();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        disablerList.clear();
        configContainerMap.clear();
        CommandManager.INSTANCE.unregisterAll();
        CrypticLib.platform().scheduler().cancelTasks(this);
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

    public void reloadPlugin() {
        reloadConfig();
        reloaderList.forEach(it -> {
            try {
                it.reload();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
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
    }

}
