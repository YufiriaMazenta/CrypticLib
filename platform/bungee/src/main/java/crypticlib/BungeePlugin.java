package crypticlib;

import crypticlib.chat.BungeeMsgSender;
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
import crypticlib.util.IOHelper;
import crypticlib.util.ReflectionHelper;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BungeePlugin extends Plugin {

    protected final PluginScanner pluginScanner = PluginScanner.INSTANCE;
    protected final Map<String, BungeeConfigContainer> configContainerMap = new ConcurrentHashMap<>();
    protected final String defaultConfigFileName = "config.yml";

    public BungeePlugin() {
        pluginScanner.scanJar(this.getFile());
        ReflectionHelper.setPluginInstance(this);
        IOHelper.setMsgSender(BungeeMsgSender.INSTANCE);
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
                    getProxy().getPluginManager().registerListener(this, listener);
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
                try {
                    if (!BungeeCommand.class.isAssignableFrom(commandClass)) {
                        return;
                    }
                    BungeeCommand bungeeCommand = (BungeeCommand) ReflectionHelper.getSingletonClassInstance(commandClass);
                    bungeeCommand.register(this);
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    Command annotation = commandClass.getAnnotation(Command.class);
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
        getProxy().getScheduler().runAsync(this, () -> runLifeCycleTasks(LifeCycle.ACTIVE));
    }

    @Override
    public final void onDisable() {
        runLifeCycleTasks(LifeCycle.DISABLE);
        configContainerMap.clear();
        BungeeCommandManager.INSTANCE.unregisterAll();
        getProxy().getScheduler().cancel(this);
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
        List<BungeeLifeCycleTaskWrapper> taskWrappers = new ArrayList<>();
        pluginScanner.getAnnotatedClasses(AutoTask.class).forEach(
            taskClass -> {
                try {
                    if (!BungeeLifeCycleTask.class.isAssignableFrom(taskClass)) {
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
                            BungeeLifeCycleTask task = (BungeeLifeCycleTask) ReflectionHelper.getSingletonClassInstance(taskClass);
                            BungeeLifeCycleTaskWrapper wrapper = new BungeeLifeCycleTaskWrapper(task, priority);
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
        taskWrappers.sort(Comparator.comparingInt(BungeeLifeCycleTaskWrapper::priority));
        for (BungeeLifeCycleTaskWrapper taskWrapper : taskWrappers) {
            taskWrapper.runLifecycleTask(this, lifeCycle);
        }
    }

}
