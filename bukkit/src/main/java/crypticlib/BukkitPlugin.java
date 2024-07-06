package crypticlib;

import com.electronwill.nightconfig.core.file.FileConfig;
import crypticlib.chat.MsgSender;
import crypticlib.command.CommandHandler;
import crypticlib.command.annotation.Command;
import crypticlib.command.manager.CommandManager;
import crypticlib.config.ConfigContainer;
import crypticlib.config.ConfigHandler;
import crypticlib.config.ConfigWrapper;
import crypticlib.internal.PluginScanner;
import crypticlib.lifecycle.Disabler;
import crypticlib.lifecycle.Reloader;
import crypticlib.listener.BukkitListener;
import crypticlib.util.ReflectUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

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
        ConfigWrapper.dataFolder = getDataFolder();
        PluginScanner pluginScanner = PluginScanner.INSTANCE;
        pluginScanner.scanJar(this.getFile());
        pluginScanner.getAnnotatedClasses(ConfigHandler.class).forEach(
            configClass -> {
                ConfigHandler configHandler = configClass.getAnnotation(ConfigHandler.class);
                String path = configHandler.path();
                if (!path.endsWith(".yml") && !path.endsWith(".yaml"))
                    path += ".yml";
                ConfigWrapper configWrapper = new ConfigWrapper(path);
                ConfigContainer configContainer = new ConfigContainer(configClass, configWrapper);
                configContainerMap.put(path, configContainer);
                configContainer.reload();
            }
        );
        pluginScanner.getAnnotatedClasses(BukkitListener.class).forEach(
            listenerClass -> {
                try {
                    if (!Listener.class.isAssignableFrom(listenerClass)) {
                        return;
                    }
                    Listener listener = (Listener) ReflectUtil.getSingletonClassInstance(listenerClass);
                    Bukkit.getPluginManager().registerEvents(listener, this);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        );
        pluginScanner.getAnnotatedClasses(Command.class).forEach(
            commandClass -> {
                try {
                    if (!CommandHandler.class.isAssignableFrom(commandClass)) {
                        MsgSender.info("&e@Command annotation is used on non-CommandHandler implementation class:" + commandClass.getName());
                        return;
                    }
                    CommandHandler commandHandler = (CommandHandler) ReflectUtil.getSingletonClassInstance(commandClass);
                    commandHandler.register(this);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        );
//        pluginScanner.getAnnotatedClasses(ConfigHandler.class).forEach()
//            .regClassAnnotationProcessor(
//                ConfigHandler.class,
//                (annotation, clazz) -> {
//                    ConfigHandler configHandler = (ConfigHandler) annotation;
//                    String path = configHandler.path();
//                    if (!path.endsWith(".yml") && !path.endsWith(".yaml"))
//                        path += ".yml";
//                    ConfigWrapper configWrapper = new ConfigWrapper(this, path);
//                    ConfigContainer configContainer = new ConfigContainer(clazz, configWrapper);
//                    configContainerMap.put(path, configContainer);
//                    configContainer.reload();
//                }, PluginScanner.ProcessPriority.LOWEST)
//            .regClassAnnotationProcessor(
//                LangHandler.class,
//                (annotation, clazz) -> {
//                    LangHandler langHandler = (LangHandler) annotation;
//                    String langFileFolder = langHandler.langFileFolder();
//                    String defLang = langHandler.defLang();
//                    LangEntryContainer langEntryContainer = new LangEntryContainer(this, clazz, langFileFolder, defLang);
//                    LangManager.INSTANCE.loadLangEntryContainer(langFileFolder, langEntryContainer);
//                }, PluginScanner.ProcessPriority.LOWEST)
//            .regClassAnnotationProcessor(
//                OnDisable.class,
//                (annotation, clazz) -> {
//                    if (!Disabler.class.isAssignableFrom(clazz))
//                        return;
//                    try {
//                        Disabler disabler = (Disabler) pluginScanner.getClassInstance(clazz);
//                        disablerList.add(disabler);
//                    } catch (ClassNotFoundException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            )
//            .regClassAnnotationProcessor(
//                OnReload.class,
//                (annotation, clazz) -> {
//                    if (!Reloader.class.isAssignableFrom(clazz))
//                        return;
//                    try {
//                        Reloader reloader = (Reloader) pluginScanner.getClassInstance(clazz);
//                        reloaderList.add(reloader);
//                    } catch (ClassNotFoundException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            );
        load();
    }

    @Override
    public final void onEnable() {
        PluginScanner.INSTANCE.scanJar(getFile());
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
        CrypticLibBukkit.platform().scheduler().cancelTasks(this);
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

    public @NotNull FileConfig getDefaultConfig() {
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
        ConfigContainer defConfig = new ConfigContainer(this.getClass(), new ConfigWrapper(defaultConfigFileName));
        defConfig.reload();
        configContainerMap.put(defaultConfigFileName, defConfig);
    }

    @Override
    public void reloadConfig() {
        configContainerMap.forEach((path, container) -> container.reload());
    }

}
