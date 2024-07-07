package crypticlib;

import crypticlib.command.BukkitCommand;
import crypticlib.config.BukkitConfigContainer;
import crypticlib.config.BukkitConfigWrapper;
import crypticlib.command.manager.BukkitCommandManager;
import crypticlib.command.annotation.Command;
import crypticlib.config.ConfigHandler;
import crypticlib.internal.PluginScanner;
import crypticlib.internal.exception.PluginEnableException;
import crypticlib.internal.exception.PluginLoadException;
import crypticlib.lifecycle.Disabler;
import crypticlib.lifecycle.Enabler;
import crypticlib.lifecycle.Loader;
import crypticlib.lifecycle.Reloader;
import crypticlib.lifecycle.annotation.OnDisable;
import crypticlib.lifecycle.annotation.OnEnable;
import crypticlib.lifecycle.annotation.OnLoad;
import crypticlib.lifecycle.annotation.OnReload;
import crypticlib.listener.BukkitListener;
import crypticlib.perm.BukkitPermManager;
import crypticlib.perm.PermInfo;
import crypticlib.util.ReflectionHelper;
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

    protected final Map<String, BukkitConfigContainer> configContainerMap = new ConcurrentHashMap<>();
    protected final List<Disabler> disablerList = new CopyOnWriteArrayList<>();
    protected final List<Loader> loaderList = new CopyOnWriteArrayList<>();
    protected final List<Enabler> enablerList = new CopyOnWriteArrayList<>();
    protected final List<Reloader> reloaderList = new CopyOnWriteArrayList<>();
    protected final String defaultConfigFileName = "config.yml";

    protected BukkitPlugin() {
        super();
    }

    @Override
    public final void onLoad() {
        PermInfo.PERM_MANAGER = BukkitPermManager.INSTANCE;
        PluginScanner pluginScanner = PluginScanner.INSTANCE;
        pluginScanner.scanJar(this.getFile());
        pluginScanner.getAnnotatedClasses(ConfigHandler.class).forEach(
            configClass -> {
                ConfigHandler configHandler = configClass.getAnnotation(ConfigHandler.class);
                String path = configHandler.path();
                if (!path.endsWith(".yml") && !path.endsWith(".yaml"))
                    path += ".yml";
                BukkitConfigWrapper configWrapper = new BukkitConfigWrapper(this, path);
                BukkitConfigContainer configContainer = new BukkitConfigContainer(configClass, configWrapper);
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
                    Listener listener = (Listener) ReflectionHelper.getSingletonClassInstance(listenerClass);
                    Bukkit.getPluginManager().registerEvents(listener, this);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        );
        pluginScanner.getAnnotatedClasses(Command.class).forEach(
            commandClass -> {
                try {
                    if (!BukkitCommand.class.isAssignableFrom(commandClass)) {
//                        MsgSender.info("&e@Command annotation is used on non-CommandHandler implementation class:" + commandClass.getName());
                        return;
                    }
                    BukkitCommand bukkitCommand = (BukkitCommand) ReflectionHelper.getSingletonClassInstance(commandClass);
                    bukkitCommand.register(this);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        );
        loaderList.clear();
        pluginScanner.getAnnotatedClasses(OnLoad.class).forEach(
            loaderClass -> {
                try {
                    if (!Loader.class.isAssignableFrom(loaderClass)) {
                        return;
                    }
                    Loader loader = (Loader) ReflectionHelper.getSingletonClassInstance(loaderClass);
                    loaderList.add(loader);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        );
        enablerList.clear();
        pluginScanner.getAnnotatedClasses(OnEnable.class).forEach(
            enablerClass -> {
                try {
                    if (!Enabler.class.isAssignableFrom(enablerClass)) {
                        return;
                    }
                    Enabler enabler = (Enabler) ReflectionHelper.getSingletonClassInstance(enablerClass);
                    enablerList.add(enabler);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        );
        reloaderList.clear();
        pluginScanner.getAnnotatedClasses(OnReload.class).forEach(
            reloaderClass -> {
                try {
                    if (!Reloader.class.isAssignableFrom(reloaderClass)) {
                        return;
                    }
                    Reloader reloader = (Reloader) ReflectionHelper.getSingletonClassInstance(reloaderClass);
                    reloaderList.add(reloader);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        );
        disablerList.clear();
        pluginScanner.getAnnotatedClasses(OnDisable.class).forEach(
            disablerClass -> {
                try {
                    if (!Disabler.class.isAssignableFrom(disablerClass)) {
                        return;
                    }
                    Disabler disabler = (Disabler) ReflectionHelper.getSingletonClassInstance(disablerClass);
                    disablerList.add(disabler);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        );
        loaderList.forEach(
            loader -> {
                try {
                    loader.load(this);
                } catch (Throwable throwable) {
                    throw new PluginLoadException(throwable);
                }
            }
        );
        load();
    }

    @Override
    public final void onEnable() {
        enablerList.forEach(
            enabler -> {
                try {
                    enabler.enable(this);
                } catch (Throwable throwable) {
                    throw new PluginEnableException(throwable);
                }
            }
        );
        enable();
    }

    @Override
    public final void onDisable() {
        disable();
        loaderList.clear();
        enablerList.clear();
        reloaderList.clear();
        disablerList.forEach(it -> {
            try {
                it.disable(this);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        disablerList.clear();
        configContainerMap.clear();
        BukkitCommandManager.INSTANCE.unregisterAll();
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
                it.reload(this);
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
        BukkitConfigContainer defConfig = new BukkitConfigContainer(this.getClass(), new BukkitConfigWrapper(this, defaultConfigFileName));
        defConfig.reload();
        configContainerMap.put(defaultConfigFileName, defConfig);
    }

    @Override
    public void reloadConfig() {
        configContainerMap.forEach((path, container) -> container.reload());
    }

}
