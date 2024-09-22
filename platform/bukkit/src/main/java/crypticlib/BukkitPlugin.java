package crypticlib;

import crypticlib.command.BukkitCommand;
import crypticlib.command.BukkitCommandManager;
import crypticlib.command.annotation.Command;
import crypticlib.config.BukkitConfigContainer;
import crypticlib.config.BukkitConfigWrapper;
import crypticlib.config.ConfigHandler;
import crypticlib.internal.PluginScanner;
import crypticlib.lifecycle.*;
import crypticlib.listener.EventListener;
import crypticlib.perm.BukkitPermManager;
import crypticlib.perm.PermInfo;
import crypticlib.util.ReflectionHelper;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class BukkitPlugin extends JavaPlugin {

    protected final PluginScanner pluginScanner = PluginScanner.INSTANCE;
    protected final Map<String, BukkitConfigContainer> configContainerMap = new ConcurrentHashMap<>();
    protected final Map<LifeCycle, List<BukkitLifeCycleTaskWrapper>> lifeCycleTaskMap = new ConcurrentHashMap<>();
    protected final String defaultConfigFileName = "config.yml";

    public BukkitPlugin() {
        pluginScanner.scanJar(this.getFile());
        lifeCycleTaskMap.clear();
        pluginScanner.getAnnotatedClasses(AutoTask.class).forEach(
            taskClass -> {
                try {
                    if (!BukkitLifeCycleTask.class.isAssignableFrom(taskClass)) {
                        return;
                    }
                    BukkitLifeCycleTask task = (BukkitLifeCycleTask) ReflectionHelper.getSingletonClassInstance(taskClass);
                    AutoTask annotation = taskClass.getAnnotation(AutoTask.class);
                    if (annotation == null) {
                        return;
                    }
                    for (TaskRule taskRule : annotation.rules()) {
                        LifeCycle lifeCycle = taskRule.lifeCycle();
                        BukkitLifeCycleTaskWrapper wrapper = new BukkitLifeCycleTaskWrapper(task, taskRule.priority());
                        if (lifeCycleTaskMap.containsKey(lifeCycle)) {
                            lifeCycleTaskMap.get(lifeCycle).add(wrapper);
                        } else {
                            List<BukkitLifeCycleTaskWrapper> taskWrappers = new CopyOnWriteArrayList<>();
                            taskWrappers.add(wrapper);
                            lifeCycleTaskMap.put(lifeCycle, taskWrappers);
                        }
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        );
        lifeCycleTaskMap.forEach((lifeCycle, taskWrappers) -> {
            taskWrappers.sort(Comparator.comparingInt(LifeCycleTaskWrapper::priority));
        });

        runLifeCycleTasks(LifeCycle.INIT);
    }

    @Override
    public final void onLoad() {
        PermInfo.PERM_MANAGER = BukkitPermManager.INSTANCE;
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
        runLifeCycleTasks(LifeCycle.LOAD);
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
                        return;
                    }
                    BukkitCommand bukkitCommand = (BukkitCommand) ReflectionHelper.getSingletonClassInstance(commandClass);
                    bukkitCommand.register(this);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        );
        runLifeCycleTasks(LifeCycle.ENABLE);
        enable();
    }

    @Override
    public final void onDisable() {
        disable();
        runLifeCycleTasks(LifeCycle.DISABLE);
        lifeCycleTaskMap.clear();
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
        runLifeCycleTasks(LifeCycle.RELOAD);
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

    private void runLifeCycleTasks(LifeCycle lifeCycle) {
        List<BukkitLifeCycleTaskWrapper> lifeCycleTasks = lifeCycleTaskMap.get(lifeCycle);
        if (lifeCycleTasks != null) {
            lifeCycleTasks.forEach(it -> it.run(this, lifeCycle));
        }
    }

}
