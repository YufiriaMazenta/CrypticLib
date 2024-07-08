package crypticlib;

import crypticlib.command.BungeeCommand;
import crypticlib.command.BungeeCommandManager;
import crypticlib.command.annotation.Command;
import crypticlib.config.BungeeConfigContainer;
import crypticlib.config.BungeeConfigWrapper;
import crypticlib.config.ConfigHandler;
import crypticlib.internal.PluginScanner;
import crypticlib.internal.exception.PluginEnableException;
import crypticlib.internal.exception.PluginLoadException;
import crypticlib.lifecycle.BungeeDisabler;
import crypticlib.lifecycle.BungeeEnabler;
import crypticlib.lifecycle.BungeeLoader;
import crypticlib.lifecycle.BungeeReloader;
import crypticlib.lifecycle.annotation.OnDisable;
import crypticlib.lifecycle.annotation.OnEnable;
import crypticlib.lifecycle.annotation.OnLoad;
import crypticlib.lifecycle.annotation.OnReload;
import crypticlib.listener.EventListener;
import crypticlib.perm.BungeePermManager;
import crypticlib.perm.PermInfo;
import crypticlib.util.ReflectionHelper;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class BungeePlugin extends Plugin {

    protected final PluginScanner pluginScanner = PluginScanner.INSTANCE;
    protected final Map<String, BungeeConfigContainer> configContainerMap = new ConcurrentHashMap<>();
    protected final List<BungeeDisabler> disablerList = new CopyOnWriteArrayList<>();
    protected final List<BungeeLoader> loaderList = new CopyOnWriteArrayList<>();
    protected final List<BungeeEnabler> enablerList = new CopyOnWriteArrayList<>();
    protected final List<BungeeReloader> reloaderList = new CopyOnWriteArrayList<>();
    protected final String defaultConfigFileName = "config.yml";

    @Override
    public final void onLoad() {
        PermInfo.PERM_MANAGER = BungeePermManager.INSTANCE;
        pluginScanner.scanJar(this.getFile());
        pluginScanner.getAnnotatedClasses(ConfigHandler.class).forEach(
            configClass -> {
                ConfigHandler configHandler = configClass.getAnnotation(ConfigHandler.class);
                String path = configHandler.path();
                if (!path.endsWith(".yml") && !path.endsWith(".yaml") && !path.endsWith(".json"))
                    path += ".yml";
                BungeeConfigWrapper configWrapper = new BungeeConfigWrapper(this, path);
                BungeeConfigContainer configContainer = new BungeeConfigContainer(configClass, configWrapper);
                configContainerMap.put(path, configContainer);
                configContainer.reload();
            }
        );
        loaderList.clear();
        pluginScanner.getAnnotatedClasses(OnLoad.class).forEach(
            loaderClass -> {
                try {
                    if (!BungeeLoader.class.isAssignableFrom(loaderClass)) {
                        return;
                    }
                    BungeeLoader loader = (BungeeLoader) ReflectionHelper.getSingletonClassInstance(loaderClass);
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
                    if (!BungeeEnabler.class.isAssignableFrom(enablerClass)) {
                        return;
                    }
                    BungeeEnabler enabler = (BungeeEnabler) ReflectionHelper.getSingletonClassInstance(enablerClass);
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
                    if (!BungeeReloader.class.isAssignableFrom(reloaderClass)) {
                        return;
                    }
                    BungeeReloader reloader = (BungeeReloader) ReflectionHelper.getSingletonClassInstance(reloaderClass);
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
                    if (!BungeeDisabler.class.isAssignableFrom(disablerClass)) {
                        return;
                    }
                    BungeeDisabler disabler = (BungeeDisabler) ReflectionHelper.getSingletonClassInstance(disablerClass);
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
        pluginScanner.getAnnotatedClasses(EventListener.class).forEach(
            listenerClass -> {
                try {
                    if (!Listener.class.isAssignableFrom(listenerClass)) {
                        return;
                    }
                    Listener listener = (Listener) ReflectionHelper.getSingletonClassInstance(listenerClass);
                    getProxy().getPluginManager().registerListener(this, listener);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        );
        pluginScanner.getAnnotatedClasses(Command.class).forEach(
            commandClass -> {
                try {
                    if (!BungeeCommand.class.isAssignableFrom(commandClass)) {
                        return;
                    }
                    BungeeCommand BungeeCommand = (BungeeCommand) ReflectionHelper.getSingletonClassInstance(commandClass);
                    BungeeCommand.register(this);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        );
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
        BungeeCommandManager.INSTANCE.unregisterAll();
        getProxy().getScheduler().cancel(this);
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
    
    public @NotNull Configuration getConfig() {
        if (configContainerMap.containsKey(defaultConfigFileName)) {
            return configContainerMap.get(defaultConfigFileName).configWrapper().config();
        }
        throw new UnsupportedOperationException("No default config file");
    }
    
    public void saveConfig() {
        configContainerMap.forEach((path, configContainer) -> configContainer.configWrapper().saveConfig());
    }
    
    public void saveDefaultConfig() {
        BungeeConfigContainer defConfig = new BungeeConfigContainer(this.getClass(), new BungeeConfigWrapper(this, defaultConfigFileName));
        defConfig.reload();
        configContainerMap.put(defaultConfigFileName, defConfig);
    }

    public void reloadConfig() {
        configContainerMap.forEach((path, container) -> container.reload());
    }


}
