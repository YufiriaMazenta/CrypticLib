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
    protected final Map<Class<?>, VelocityConfigContainer> configContainerMap = new ConcurrentHashMap<>();
    protected final Map<String, VelocityConfigWrapper> configWrapperMap = new ConcurrentHashMap<>();

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
                if (!Arrays.asList(configHandler.platforms()).contains(PlatformSide.VELOCITY)) {
                    return;
                }

                String path = configHandler.path();
                if (!path.endsWith(".yml") && !path.endsWith(".yaml"))
                    path += ".yml";
                VelocityConfigWrapper configWrapper = getConfigWrapperOrCreate(path);
                VelocityConfigContainer configContainer = new VelocityConfigContainer(configClass, configWrapper);
                configContainerMap.put(configClass, configContainer);
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
        configWrapperMap.forEach((path, wrapper) -> wrapper.reloadConfig());
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
    
    /**
     * 获取插件的配置文件
     * @param path 配置文件相对于插件文件夹的路径
     */
    public final Optional<VelocityConfigWrapper> getConfigWrapper(String path) {
        return Optional.ofNullable(configWrapperMap.get(path));
    }

    /**
     * 获取插件的配置文件,如果不存在时会创建文件
     * @param path 配置文件相对于插件文件夹的路径
     */
    public final VelocityConfigWrapper getConfigWrapperOrCreate(String path) {
        Optional<VelocityConfigWrapper> configWrapperOpt = getConfigWrapper(path);
        return configWrapperOpt.orElseGet(() -> {
            if (!path.endsWith(".yml") && !path.endsWith(".yaml")) {
                throw new UnsupportedOperationException("Velocity only support yaml config");
            }
            VelocityConfigWrapper configWrapper = new VelocityConfigWrapper(this, path);
            configWrapperMap.put(path, configWrapper);
            return configWrapper;
        });
    }

    /**
     * 删除插件缓存的一个配置文件
     * 被删除的配置文件将不会在reload时自动重载
     * 请谨慎操作
     * @param path 配置文件相对于插件文件夹的路径
     * @return 被删除掉的配置文件包装
     */
    public final VelocityConfigWrapper removeConfigWrapper(String path) {
        return configWrapperMap.remove(path);
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
                            List<Class<? extends Throwable>> ignoreExceptions = Arrays.asList(annotation.ignoreExceptions());
                            List<Class<? extends Throwable>> printExceptions = Arrays.asList(annotation.printExceptions());
                            VelocityLifeCycleTaskWrapper wrapper = new VelocityLifeCycleTaskWrapper(task, priority, ignoreExceptions, printExceptions);
                            taskWrappers.add(wrapper);
                            return;
                        }
                    }
                } catch (Throwable throwable) {
                    AutoTask annotation = taskClass.getAnnotation(AutoTask.class);
                    List<Class<? extends Throwable>> ignoreExceptions = Arrays.asList(annotation.ignoreExceptions());
                    if (ignoreExceptions.contains(throwable.getClass())) {
                        return;
                    }
                    List<Class<? extends Throwable>> printExceptions = Arrays.asList(annotation.printExceptions());
                    if (printExceptions.contains(throwable.getClass())) {
                        throwable.printStackTrace();
                        return;
                    }
                    throw new RuntimeException(throwable);
                }
            }
        );
        taskWrappers.sort(Comparator.comparingInt(VelocityLifeCycleTaskWrapper::priority));
        for (VelocityLifeCycleTaskWrapper taskWrapper : taskWrappers) {
            taskWrapper.runLifecycleTask(this, lifeCycle);
        }
    }

}
