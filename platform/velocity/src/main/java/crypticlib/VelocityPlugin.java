package crypticlib;

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
import crypticlib.lifecycle.*;
import crypticlib.listener.EventListener;
import crypticlib.perm.PermInfo;
import crypticlib.perm.VelocityPermManager;
import crypticlib.util.ReflectionHelper;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class VelocityPlugin {

    protected final PluginScanner pluginScanner = PluginScanner.INSTANCE;
    protected final Logger logger;
    protected final Path dataDirectory;
    protected final PluginContainer pluginContainer;
    protected final ProxyServer proxyServer;
    protected final Map<String, VelocityConfigContainer> configContainerMap = new ConcurrentHashMap<>();

    public VelocityPlugin(Logger logger, ProxyServer proxyServer, PluginContainer pluginContainer, Path dataDirectory) {
        this.logger = logger;
        this.proxyServer = proxyServer;
        this.pluginContainer = pluginContainer;
        this.dataDirectory = dataDirectory;
        File pluginFile = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
        pluginScanner.scanJar(pluginFile);
        ReflectionHelper.setPluginInstance(this);
        runLifeCycleTasks(LifeCycle.INIT);
    }

    @Subscribe
    public final void onProxyInitialization(ProxyInitializeEvent event) {
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
        load();
        runLifeCycleTasks(LifeCycle.LOAD);

        //Enable 阶段
        pluginScanner.getAnnotatedClasses(EventListener.class).forEach(
            listenerClass -> {
                try {
                    Object listener = ReflectionHelper.getSingletonClassInstance(listenerClass);
                    proxyServer.getEventManager().register(this, listener);
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
                    if (!VelocityCommand.class.isAssignableFrom(commandClass)) {
                        return;
                    }
                    VelocityCommand command = (VelocityCommand) ReflectionHelper.getSingletonClassInstance(commandClass);
                    command.register(this);
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
        proxyServer.getScheduler().buildTask(this, () -> runLifeCycleTasks(LifeCycle.ACTIVE)).schedule();
    }

    @Subscribe
    public final void onProxyShutdown(ProxyShutdownEvent event) {
        runLifeCycleTasks(LifeCycle.DISABLE);
        configContainerMap.clear();
        VelocityCommandManager.INSTANCE.unregisterAll();
        for (ScheduledTask scheduledTask : proxyServer.getScheduler().tasksByPlugin(this)) {
            scheduledTask.cancel();
        }
        disable();
    }

    public void load() {}

    public void enable() {}

    public void disable() {}

    public final void reloadPlugin() {
        reloadConfig();
        runLifeCycleTasks(LifeCycle.RELOAD);
    }

    public final void reloadConfig() {
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
        List<VelocityLifeCycleTaskWrapper> taskWrappers = new ArrayList<>();
        pluginScanner.getAnnotatedClasses(AutoTask.class).forEach(
            taskClass -> {
                try {
                    if (!VelocityLifeCycleTask.class.isAssignableFrom(taskClass)) {
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
                            VelocityLifeCycleTask task = (VelocityLifeCycleTask) ReflectionHelper.getSingletonClassInstance(taskClass);
                            VelocityLifeCycleTaskWrapper wrapper = new VelocityLifeCycleTaskWrapper(task, priority);
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
        taskWrappers.sort(Comparator.comparingInt(VelocityLifeCycleTaskWrapper::priority));
        for (VelocityLifeCycleTaskWrapper taskWrapper : taskWrappers) {
            taskWrapper.run(this, lifeCycle);
        }
    }

}
