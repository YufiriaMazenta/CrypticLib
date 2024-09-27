package crypticlib;

import com.electronwill.nightconfig.core.file.FormatDetector;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import crypticlib.command.VelocityCommand;
import crypticlib.command.VelocityCommandManager;
import crypticlib.command.annotation.Command;
import crypticlib.config.ConfigHandler;
import crypticlib.config.VelocityConfigContainer;
import crypticlib.config.VelocityConfigWrapper;
import crypticlib.internal.PluginScanner;
import crypticlib.internal.config.yaml.YamlFormat;
import crypticlib.lifecycle.*;
import crypticlib.listener.EventListener;
import crypticlib.perm.PermInfo;
import crypticlib.perm.VelocityPermManager;
import crypticlib.util.ReflectionHelper;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class VelocityPlugin {

    protected final PluginScanner pluginScanner = PluginScanner.INSTANCE;
    protected final Logger logger;
    protected final Path dataDirectory;
    protected final PluginContainer pluginContainer;
    protected final ProxyServer proxyServer;
    protected final Map<LifeCycle, List<VelocityLifeCycleTaskWrapper>> lifeCycleTaskMap = new ConcurrentHashMap<>();
    protected final Map<String, VelocityConfigContainer> configContainerMap = new ConcurrentHashMap<>();

    public VelocityPlugin(Logger logger, ProxyServer proxyServer, PluginContainer pluginContainer, Path dataDirectory) {
        this.logger = logger;
        this.proxyServer = proxyServer;
        this.pluginContainer = pluginContainer;
        this.dataDirectory = dataDirectory;
        File pluginFile = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
        pluginScanner.scanJar(pluginFile);
        lifeCycleTaskMap.clear();

        pluginScanner.getAnnotatedClasses(AutoTask.class).forEach(
            taskClass -> {
                try {
                    if (!VelocityLifeCycleTask.class.isAssignableFrom(taskClass)) {
                        return;
                    }
                    VelocityLifeCycleTask task;
                    if (VelocityPlugin.class.isAssignableFrom(taskClass)) {
                        task = (VelocityLifeCycleTask) this;
                    } else {
                        task = (VelocityLifeCycleTask) ReflectionHelper.getSingletonClassInstance(taskClass);
                    }
                    AutoTask annotation = taskClass.getAnnotation(AutoTask.class);
                    if (annotation == null) {
                        return;
                    }
                    for (TaskRule taskRule : annotation.rules()) {
                        LifeCycle lifeCycle = taskRule.lifeCycle();
                        VelocityLifeCycleTaskWrapper wrapper = new VelocityLifeCycleTaskWrapper(task, taskRule.priority());
                        if (lifeCycleTaskMap.containsKey(lifeCycle)) {
                            lifeCycleTaskMap.get(lifeCycle).add(wrapper);
                        } else {
                            List<VelocityLifeCycleTaskWrapper> taskWrappers = new CopyOnWriteArrayList<>();
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

    @Subscribe
    public final void onProxyInitialization(ProxyInitializeEvent event) {
        //Load 阶段
        FormatDetector.registerExtension("yaml", YamlFormat.INSTANCE);
        FormatDetector.registerExtension("yml", YamlFormat.INSTANCE);
        PermInfo.PERM_MANAGER = VelocityPermManager.INSTANCE;
        pluginScanner.getAnnotatedClasses(ConfigHandler.class).forEach(
            configClass -> {
                ConfigHandler configHandler = configClass.getAnnotation(ConfigHandler.class);
                String path = configHandler.path();
                if (!path.endsWith(".yml") && !path.endsWith(".yaml"))
                    path += ".yml";
                VelocityConfigWrapper configWrapper = new VelocityConfigWrapper(this, path);
                VelocityConfigContainer configContainer = new VelocityConfigContainer(configClass, configWrapper);
                configContainerMap.put(configClass.getName(), configContainer);
                configContainer.reload();
            }
        );
        runLifeCycleTasks(LifeCycle.LOAD);
        load();

        //Enable 阶段
        pluginScanner.getAnnotatedClasses(EventListener.class).forEach(
            listenerClass -> {
                try {
                    Object listener;
                    if (VelocityPlugin.class.isAssignableFrom(listenerClass)) {
                        listener = this;
                    } else {
                        listener = ReflectionHelper.getSingletonClassInstance(listenerClass);
                    }
                    proxyServer.getEventManager().register(this, listener);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        );

        pluginScanner.getAnnotatedClasses(Command.class).forEach(
            commandClass -> {
                try {
                    if (!VelocityCommand.class.isAssignableFrom(commandClass)) {
                        return;
                    }
                    VelocityCommand command = (VelocityCommand) ReflectionHelper.getSingletonClassInstance(commandClass);
                    command.register(this);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        );
        runLifeCycleTasks(LifeCycle.ENABLE);
    }

    @Subscribe
    public final void onProxyShutdown(ProxyShutdownEvent event) {
        disable();
        runLifeCycleTasks(LifeCycle.DISABLE);
        lifeCycleTaskMap.clear();
        configContainerMap.clear();
        VelocityCommandManager.INSTANCE.unregisterAll();
        for (ScheduledTask scheduledTask : proxyServer.getScheduler().tasksByPlugin(this)) {
            scheduledTask.cancel();
        }
    }

    public void load() {}

    public void enable() {}

    public void disable() {}

    public final void reloadPlugin() {
        reloadConfig();
        runLifeCycleTasks(LifeCycle.RELOAD);
    }

    public final void reloadConfig() {
        FormatDetector.registerExtension("yaml", YamlFormat.INSTANCE);
        FormatDetector.registerExtension("yml", YamlFormat.INSTANCE);
        configContainerMap.forEach((path, container) -> container.reload());
    }

    public PluginContainer plugin() {
        return pluginContainer;
    }

    public ProxyServer proxyServer() {
        return proxyServer;
    }

    public Logger logger() {
        return logger;
    }

    public File dataFolder() {
        return dataDirectory.toFile();
    }

    private void runLifeCycleTasks(LifeCycle lifeCycle) {
        List<VelocityLifeCycleTaskWrapper> lifeCycleTasks = lifeCycleTaskMap.get(lifeCycle);
        if (lifeCycleTasks != null) {
            lifeCycleTasks.forEach(it -> it.run(this, lifeCycle));
        }
    }

}
