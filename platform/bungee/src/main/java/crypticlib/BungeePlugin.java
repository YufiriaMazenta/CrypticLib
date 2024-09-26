package crypticlib;

import crypticlib.command.BungeeCommand;
import crypticlib.command.BungeeCommandManager;
import crypticlib.command.annotation.Command;
import crypticlib.config.BungeeConfigContainer;
import crypticlib.config.BungeeConfigWrapper;
import crypticlib.config.ConfigHandler;
import crypticlib.internal.PluginScanner;
import crypticlib.lifecycle.*;
import crypticlib.listener.EventListener;
import crypticlib.perm.BungeePermManager;
import crypticlib.perm.PermInfo;
import crypticlib.util.ReflectionHelper;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class BungeePlugin extends Plugin {

    protected final PluginScanner pluginScanner = PluginScanner.INSTANCE;
    protected final Map<String, BungeeConfigContainer> configContainerMap = new ConcurrentHashMap<>();
    protected final Map<LifeCycle, List<BungeeLifeCycleTaskWrapper>> lifeCycleTaskMap = new ConcurrentHashMap<>();
    protected final String defaultConfigFileName = "config.yml";

    public BungeePlugin() {
        pluginScanner.scanJar(this.getFile());
        lifeCycleTaskMap.clear();
        pluginScanner.getAnnotatedClasses(AutoTask.class).forEach(
            taskClass -> {
                try {
                    if (!BungeeLifeCycleTask.class.isAssignableFrom(taskClass)) {
                        return;
                    }
                    BungeeLifeCycleTask task = (BungeeLifeCycleTask) ReflectionHelper.getSingletonClassInstance(taskClass);
                    AutoTask annotation = taskClass.getAnnotation(AutoTask.class);
                    if (annotation == null) {
                        return;
                    }
                    for (TaskRule taskRule : annotation.rules()) {
                        LifeCycle lifeCycle = taskRule.lifeCycle();
                        BungeeLifeCycleTaskWrapper wrapper = new BungeeLifeCycleTaskWrapper(task, taskRule.priority());
                        if (lifeCycleTaskMap.containsKey(lifeCycle)) {
                            lifeCycleTaskMap.get(lifeCycle).add(wrapper);
                        } else {
                            List<BungeeLifeCycleTaskWrapper> taskWrappers = new CopyOnWriteArrayList<>();
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
        PermInfo.PERM_MANAGER = BungeePermManager.INSTANCE;

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
                    BungeeCommand bungeeCommand = (BungeeCommand) ReflectionHelper.getSingletonClassInstance(commandClass);
                    bungeeCommand.register(this);
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

    public final void reloadPlugin() {
        reloadConfig();
        runLifeCycleTasks(LifeCycle.RELOAD);
    }
    
    public final @NotNull Configuration getConfig() {
        if (configContainerMap.containsKey(defaultConfigFileName)) {
            return configContainerMap.get(defaultConfigFileName).configWrapper().config();
        }
        throw new UnsupportedOperationException("No default config file");
    }
    
    public final void saveConfig() {
        configContainerMap.forEach((path, configContainer) -> configContainer.configWrapper().saveConfig());
    }
    
    public final void saveDefaultConfig() {
        BungeeConfigContainer defConfig = new BungeeConfigContainer(this.getClass(), new BungeeConfigWrapper(this, defaultConfigFileName));
        defConfig.reload();
        configContainerMap.put(defaultConfigFileName, defConfig);
    }

    public final void reloadConfig() {
        configContainerMap.forEach((path, container) -> container.reload());
    }

    private void runLifeCycleTasks(LifeCycle lifeCycle) {
        List<BungeeLifeCycleTaskWrapper> lifeCycleTasks = lifeCycleTaskMap.get(lifeCycle);
        if (lifeCycleTasks != null) {
            lifeCycleTasks.forEach(it -> it.run(this, lifeCycle));
        }
    }

}
