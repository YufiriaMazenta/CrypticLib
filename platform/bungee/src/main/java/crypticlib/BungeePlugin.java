package crypticlib;

import crypticlib.chat.BungeeMsgSender;
import crypticlib.command.BungeeCommandManager;
import crypticlib.command.CommandTree;
import crypticlib.command.annotation.Command;
import crypticlib.config.BungeeConfigContainer;
import crypticlib.config.BungeeConfigWrapper;
import crypticlib.config.ConfigHandler;
import crypticlib.internal.PluginScanner;
import crypticlib.lifecycle.*;
import crypticlib.listener.EventListener;
import crypticlib.perm.BungeePermManager;
import crypticlib.perm.PermInfo;
import crypticlib.resource.ResourceLoader;
import crypticlib.util.IOHelper;
import crypticlib.util.ReflectionHelper;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BungeePlugin extends Plugin {

    protected final PluginScanner pluginScanner = PluginScanner.INSTANCE;
    protected final Map<Class<?>, BungeeConfigContainer> configContainerMap = new ConcurrentHashMap<>();
    protected final Map<String, BungeeConfigWrapper> configWrapperMap = new ConcurrentHashMap<>();
    protected final String defaultConfigFileName = "config.yml";

    public BungeePlugin() {
        pluginScanner.scanJar(this.getFile());
        ReflectionHelper.setPluginInstance(this);
        CrypticLib.setPluginName(getDescription().getName());
        CrypticLib.setCommandManager(BungeeCommandManager.INSTANCE);
        IOHelper.setMsgSender(BungeeMsgSender.INSTANCE);
        runLifeCycleTasks(LifeCycle.INIT);
    }

    @Override
    public final void onLoad() {
        PermInfo.PERM_MANAGER = BungeePermManager.INSTANCE;
        ResourceLoader.downloadResources(getDataFolder());
        pluginScanner.getAnnotatedClasses(ConfigHandler.class).forEach(
            configClass -> {
                ConfigHandler configHandler = configClass.getAnnotation(ConfigHandler.class);
                if (!Arrays.asList(configHandler.platforms()).contains(PlatformSide.BUNGEE)) {
                    return;
                }

                String path = configHandler.path();
                if (!path.endsWith(".yml") && !path.endsWith(".yaml") && !path.endsWith(".json"))
                    path += ".yml";
                BungeeConfigWrapper configWrapper = getConfigWrapperOrCreate(path);
                BungeeConfigContainer configContainer = new BungeeConfigContainer(configClass, configWrapper);
                configContainerMap.put(configClass, configContainer);
                configContainer.reload();
                IOHelper.debug("Loaded config file: " + path);
            }
        );
        whenLoad();
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
                    IOHelper.debug("Registered listener for class: " + listenerClass.getName());
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
                if (!Arrays.asList(annotation.platforms()).contains(PlatformSide.BUNGEE)) {
                    return;
                }
                try {
                    if (!CommandTree.class.isAssignableFrom(commandClass)) {
                        return;
                    }
                    CommandTree commandTree = (CommandTree) ReflectionHelper.getSingletonClassInstance(commandClass);
                    commandTree.register();
                    IOHelper.debug("Registered command `" + commandTree.commandInfo().name() + "`, handler class: " + commandTree.getClass().getName());
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    if (!annotation.ignoreClassNotFound()) {
                        e.printStackTrace();
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        );
        whenEnable();
        runLifeCycleTasks(LifeCycle.ENABLE);
        getProxy().getScheduler().runAsync(this, () -> runLifeCycleTasks(LifeCycle.ACTIVE));
    }

    @Override
    public final void onDisable() {
        runLifeCycleTasks(LifeCycle.DISABLE);
        configContainerMap.clear();
        BungeeCommandManager.INSTANCE.unregisterAll();
        getProxy().getScheduler().cancel(this);
        whenDisable();
    }
    
    /**
     * 插件开始加载时执行的方法
     */
    public void whenLoad() {

    }

    /**
     * 插件开始启用时执行的方法
     */
    public void whenEnable() {
    }

    /**
     * 插件卸载时执行的方法
     */
    public void whenDisable() {
    }

    /**
     * 插件重载时执行的方法,会在LifecycleTask之前执行
     */
    public void whenReload() {}

    public final void reloadPlugin() {
        reloadConfig();
        whenReload();
        runLifeCycleTasks(LifeCycle.RELOAD);
    }
    
    public final @NotNull Configuration getConfig() {
        if (configWrapperMap.containsKey(defaultConfigFileName)) {
            return configWrapperMap.get(defaultConfigFileName).config();
        }
        throw new UnsupportedOperationException("No default config file");
    }
    
    public final void saveConfig() {
        configContainerMap.forEach((path, configContainer) -> configContainer.configWrapper().saveConfig());
    }
    
    public final void saveDefaultConfig() {
        BungeeConfigContainer defConfig = new BungeeConfigContainer(this.getClass(), getConfigWrapperOrCreate(defaultConfigFileName));
        defConfig.reload();
        configContainerMap.put(this.getClass(), defConfig);
    }

    public final void reloadConfig() {
        configWrapperMap.forEach((path, wrapper) -> wrapper.reloadConfig());
        configContainerMap.forEach((path, container) -> container.reload());
    }

    /**
     * 获取插件的配置文件
     * @param path 配置文件相对于插件文件夹的路径
     */
    public final Optional<BungeeConfigWrapper> getConfigWrapper(String path) {
        return Optional.ofNullable(configWrapperMap.get(path));
    }

    /**
     * 获取插件的配置文件,如果不存在时会创建文件
     * @param path 配置文件相对于插件文件夹的路径
     */
    public final BungeeConfigWrapper getConfigWrapperOrCreate(String path) {
        Optional<BungeeConfigWrapper> configWrapperOpt = getConfigWrapper(path);
        return configWrapperOpt.orElseGet(() -> {
            if (!path.endsWith(".yml") && !path.endsWith(".yaml") && !path.endsWith(".json")) {
                throw new UnsupportedOperationException("Bungee only support yaml or json config");
            }
            BungeeConfigWrapper configWrapper = new BungeeConfigWrapper(this, path);
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
    public final BungeeConfigWrapper removeConfigWrapper(String path) {
        return configWrapperMap.remove(path);
    }
    
    private void runLifeCycleTasks(LifeCycle lifeCycle) {
        List<BungeeLifeCycleTaskWrapper> taskWrappers = new ArrayList<>();
        pluginScanner.getAnnotatedClasses(LifeCycleTaskSettings.class).forEach(
            taskClass -> {
                try {
                    if (!BungeeLifeCycleTask.class.isAssignableFrom(taskClass)) {
                        return;
                    }
                    LifeCycleTaskSettings annotation = taskClass.getAnnotation(LifeCycleTaskSettings.class);
                    if (annotation == null) {
                        return;
                    }
                    for (TaskRule taskRule : annotation.rules()) {
                        LifeCycle annotationLifeCycle = taskRule.lifeCycle();
                        int priority = taskRule.priority();
                        if (annotationLifeCycle.equals(lifeCycle)) {
                            BungeeLifeCycleTask task = (BungeeLifeCycleTask) ReflectionHelper.getSingletonClassInstance(taskClass);
                            List<Class<? extends Throwable>> ignoreExceptions = Arrays.asList(annotation.ignoreExceptions());
                            List<Class<? extends Throwable>> printExceptions = Arrays.asList(annotation.printExceptions());
                            BungeeLifeCycleTaskWrapper wrapper = new BungeeLifeCycleTaskWrapper(task, priority, ignoreExceptions, printExceptions);
                            taskWrappers.add(wrapper);
                            return;
                        }
                    }
                } catch (Throwable throwable) {
                    LifeCycleTaskSettings annotation = taskClass.getAnnotation(LifeCycleTaskSettings.class);
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
        taskWrappers.sort(Comparator.comparingInt(BungeeLifeCycleTaskWrapper::priority));
        for (BungeeLifeCycleTaskWrapper taskWrapper : taskWrappers) {
            taskWrapper.runLifecycleTask(this, lifeCycle);
        }
    }

}
