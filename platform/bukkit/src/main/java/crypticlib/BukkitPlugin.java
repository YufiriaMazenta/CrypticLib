package crypticlib;

import crypticlib.chat.BukkitMsgSender;
import crypticlib.command.BukkitCommandManager;
import crypticlib.command.CommandTree;
import crypticlib.command.annotation.Command;
import crypticlib.config.BukkitConfigContainer;
import crypticlib.config.BukkitConfigWrapper;
import crypticlib.config.ConfigHandler;
import crypticlib.internal.PluginScanner;
import crypticlib.lifecycle.*;
import crypticlib.listener.EventListener;
import crypticlib.perm.BukkitPermManager;
import crypticlib.perm.PermInfo;
import crypticlib.resource.ResourceLoader;
import crypticlib.util.IOHelper;
import crypticlib.util.ReflectionHelper;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BukkitPlugin extends JavaPlugin {

    protected final PluginScanner pluginScanner = PluginScanner.INSTANCE;
    protected final Map<String, BukkitConfigContainer> configContainerMap = new ConcurrentHashMap<>();
    protected final String defaultConfigFileName = "config.yml";

    public BukkitPlugin() {
        pluginScanner.scanJar(this.getFile());
        ReflectionHelper.setPluginInstance(this);
        CrypticLib.setPluginName(getDescription().getName());
        CrypticLib.setCommandManager(BukkitCommandManager.INSTANCE);
        IOHelper.setMsgSender(BukkitMsgSender.INSTANCE);
        runLifeCycleTasks(LifeCycle.INIT);
    }

    @Override
    public final void onLoad() {
        PermInfo.PERM_MANAGER = BukkitPermManager.INSTANCE;
        ResourceLoader.downloadResources(getDataFolder());
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
        load();
        runLifeCycleTasks(LifeCycle.LOAD);
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
                    Bukkit.getPluginManager().registerEvents(listener, this);
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    EventListener annotation = listenerClass.getAnnotation(EventListener.class);
                    if (!annotation.ignoreClassNotFound()) {
                        e.printStackTrace();
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        );
        pluginScanner.getAnnotatedClasses(Command.class).forEach(
            commandClass -> {
                Command annotation = commandClass.getAnnotation(Command.class);
                if (!Arrays.asList(annotation.platforms()).contains(PlatformSide.BUKKIT)) {
                    return;
                }
                try {
                    if (!CommandTree.class.isAssignableFrom(commandClass)) {
                        return;
                    }
                    CommandTree commandTree = (CommandTree) ReflectionHelper.getSingletonClassInstance(commandClass);
                    commandTree.register();
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    if (!annotation.ignoreClassNotFound()) {
                        e.printStackTrace();
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        );
        enable();
        runLifeCycleTasks(LifeCycle.ENABLE);
        CrypticLibBukkit.scheduler().sync(() -> {
            runLifeCycleTasks(LifeCycle.ACTIVE);
        });
    }

    @Override
    public final void onDisable() {
        runLifeCycleTasks(LifeCycle.DISABLE);
        configContainerMap.clear();
        BukkitCommandManager.INSTANCE.unregisterAll();
        CrypticLibBukkit.scheduler().cancelTasks();
        disable();
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

    /**
     * 插件重载时执行的方法,会在LifecycleTask之前执行
     */
    public void reload() {}

    public final void reloadPlugin() {
        reloadConfig();
        reload();
        runLifeCycleTasks(LifeCycle.RELOAD);
    }

    @Override
    public final @NotNull FileConfiguration getConfig() {
        if (configContainerMap.containsKey(defaultConfigFileName)) {
            return configContainerMap.get(defaultConfigFileName).configWrapper().config();
        }
        throw new UnsupportedOperationException("No default config file");
    }

    @Override
    public final void saveConfig() {
        configContainerMap.forEach((path, configContainer) -> configContainer.configWrapper().saveConfig());
    }

    @Override
    public final void saveDefaultConfig() {
        BukkitConfigContainer defConfig = new BukkitConfigContainer(this.getClass(), new BukkitConfigWrapper(this, defaultConfigFileName));
        defConfig.reload();
        configContainerMap.put(defaultConfigFileName, defConfig);
    }

    @Override
    public final void reloadConfig() {
        configContainerMap.forEach((path, container) -> container.reload());
    }

    private void runLifeCycleTasks(LifeCycle lifeCycle) {
        List<BukkitLifeCycleTaskWrapper> taskWrappers = new ArrayList<>();
        pluginScanner.getAnnotatedClasses(AutoTask.class).forEach(
            taskClass -> {
                try {
                    if (!BukkitLifeCycleTask.class.isAssignableFrom(taskClass)) {
                        return;
                    }
                    AutoTask annotation = taskClass.getAnnotation(AutoTask.class);
                    if (annotation == null) {
                        return;
                    }
                    for (TaskRule taskRule : annotation.rules()) {
                        LifeCycle annotationLifeCycle = taskRule.lifeCycle();
                        int priority = taskRule.priority();
                        if (annotationLifeCycle.equals(lifeCycle)) {
                            BukkitLifeCycleTask task = (BukkitLifeCycleTask) ReflectionHelper.getSingletonClassInstance(taskClass);
                            BukkitLifeCycleTaskWrapper wrapper = new BukkitLifeCycleTaskWrapper(task, priority);
                            taskWrappers.add(wrapper);
                            return;
                        }
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    AutoTask annotation = taskClass.getAnnotation(AutoTask.class);
                    if (!annotation.ignoreClassNotFound()) {
                        e.printStackTrace();
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        );
        taskWrappers.sort(Comparator.comparingInt(BukkitLifeCycleTaskWrapper::priority));
        for (BukkitLifeCycleTaskWrapper taskWrapper : taskWrappers) {
            taskWrapper.runLifecycleTask(this, lifeCycle);
        }
    }

}
