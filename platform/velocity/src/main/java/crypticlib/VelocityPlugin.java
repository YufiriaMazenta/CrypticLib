package crypticlib;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.Scheduler;
import crypticlib.chat.VelocityMsgSender;
import crypticlib.command.CommandTree;
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
import crypticlib.resource.ResourceLoader;
import crypticlib.util.IOHelper;
import crypticlib.util.ReflectionHelper;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
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
        CrypticLib.setPluginName(pluginContainer.getDescription().getName().orElse(pluginContainer.getDescription().getId()));
        CrypticLib.setCommandManager(VelocityCommandManager.INSTANCE);
        IOHelper.setMsgSender(VelocityMsgSender.INSTANCE);
        runLifeCycleTasks(LifeCycle.INIT);
    }

    @Subscribe
    public final void onProxyInitialization(ProxyInitializeEvent event) {
        PermInfo.PERM_MANAGER = VelocityPermManager.INSTANCE;
        ResourceLoader.downloadResources(dataFolder());
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
                Command annotation = commandClass.getAnnotation(Command.class);
                if (!Arrays.asList(annotation.platforms()).contains(PlatformSide.VELOCITY)) {
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

    /**
     * 插件重载时执行的方法,会在LifecycleTask之前执行
     */
    public void reload() {}

    public final void reloadPlugin() {
        reloadConfig();
        reload();
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

    public Scheduler getScheduler() {
        return proxyServer.getScheduler();
    }

    public Optional<Player> getPlayerOpt(UUID uuid) {
        return proxyServer.getPlayer(uuid);
    }

    public Optional<Player> getPlayerOpt(String name) {
        return proxyServer.getPlayer(name);
    }

    public Collection<Player> getAllPlayers() {
        return proxyServer.getAllPlayers();
    }

    public Optional<RegisteredServer> getServerOpt(String name) {
        return proxyServer.getServer(name);
    }

    public Collection<RegisteredServer> getAllServers() {
        return proxyServer.getAllServers();
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
            taskWrapper.runLifecycleTask(this, lifeCycle);
        }
    }

}
