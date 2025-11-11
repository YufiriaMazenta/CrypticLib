package crypticlib;

import crypticlib.chat.BukkitMsgSender;
import crypticlib.command.BukkitCommandManager;
import crypticlib.command.CommandManager;
import crypticlib.command.CommandTree;
import crypticlib.command.annotation.Command;
import crypticlib.config.BukkitConfigContainer;
import crypticlib.config.BukkitConfigWrapper;
import crypticlib.config.ConfigHandler;
import crypticlib.internal.CrypticLibPlugin;
import crypticlib.internal.PluginScanner;
import crypticlib.lifecycle.*;
import crypticlib.listener.EventListener;
import crypticlib.perm.BukkitPermManager;
import crypticlib.perm.PermInfo;
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
    protected final Map<Class<?>, BukkitConfigContainer> configContainerMap = new ConcurrentHashMap<>();
    protected final Map<String, BukkitConfigWrapper> configWrapperMap = new ConcurrentHashMap<>();
    protected final String defaultConfigFileName = "config.yml";

    public BukkitPlugin() {
        pluginScanner.scanJar(this.getFile());
        ReflectionHelper.setPluginInstance(this);
        CrypticLib.init(new CrypticLibPlugin() {
            @Override
            public String pluginName() {
                return getDescription().getName();
            }

            @Override
            public CommandManager<?, ?> commandManager() {
                return BukkitCommandManager.INSTANCE;
            }

            @Override
            public PlatformSide platform() {
                return PlatformSide.BUKKIT;
            }
        });
        IOHelper.setMsgSender(BukkitMsgSender.INSTANCE);
        runLifeCycleTasks(LifeCycle.INIT);
    }

    @Override
    public final void onLoad() {
        PermInfo.PERM_MANAGER = BukkitPermManager.INSTANCE;
        pluginScanner.getAnnotatedClasses(ConfigHandler.class).forEach(
            configClass -> {
                ConfigHandler configHandler = configClass.getAnnotation(ConfigHandler.class);
                if (!Arrays.asList(configHandler.platforms()).contains(PlatformSide.BUKKIT)) {
                    return;
                }

                String path = configHandler.path();
                if (!path.endsWith(".yml") && !path.endsWith(".yaml"))
                    path += ".yml";
                BukkitConfigWrapper configWrapper = getConfigWrapperOrCreate(path);
                BukkitConfigContainer configContainer = new BukkitConfigContainer(configClass, configWrapper);
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
                    Bukkit.getPluginManager().registerEvents(listener, this);
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
                if (!Arrays.asList(annotation.platforms()).contains(PlatformSide.BUKKIT)) {
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

    @Override
    public final @NotNull FileConfiguration getConfig() {
        if (configWrapperMap.containsKey(defaultConfigFileName)) {
            return configWrapperMap.get(defaultConfigFileName).config();
        }
        throw new UnsupportedOperationException("No default config file");
    }

    @Override
    public final void saveConfig() {
        configContainerMap.forEach((path, configContainer) -> configContainer.configWrapper().saveConfig());
    }

    @Override
    public final void saveDefaultConfig() {
        BukkitConfigContainer defConfig = new BukkitConfigContainer(this.getClass(), getConfigWrapperOrCreate(defaultConfigFileName));
        defConfig.reload();
        configContainerMap.put(this.getClass(), defConfig);
    }

    @Override
    public final void reloadConfig() {
        configWrapperMap.forEach((path, wrapper) -> wrapper.reloadConfig());
        configContainerMap.forEach((path, container) -> container.reload());
    }

    /**
     * 获取插件的配置文件
     * @param path 配置文件相对于插件文件夹的路径
     */
    public final Optional<BukkitConfigWrapper> getConfigWrapper(String path) {
        return Optional.ofNullable(configWrapperMap.get(path));
    }

    /**
     * 获取插件的配置文件,如果不存在时会创建文件
     * @param path 配置文件相对于插件文件夹的路径
     */
    public final BukkitConfigWrapper getConfigWrapperOrCreate(String path) {
        Optional<BukkitConfigWrapper> configWrapperOpt = getConfigWrapper(path);
        return configWrapperOpt.orElseGet(() -> {
            if (!path.endsWith(".yml") && !path.endsWith(".yaml")) {
                throw new UnsupportedOperationException("Bukkit only support yaml config");
            }
            BukkitConfigWrapper configWrapper = new BukkitConfigWrapper(this, path);
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
    public final BukkitConfigWrapper removeConfigWrapper(String path) {
        return configWrapperMap.remove(path);
    }

    private void runLifeCycleTasks(LifeCycle lifeCycle) {
        List<BukkitLifeCycleTaskWrapper> taskWrappers = new ArrayList<>();
        pluginScanner.getAnnotatedClasses(LifeCycleTaskSettings.class).forEach(
            taskClass -> {
                try {
                    if (!BukkitLifeCycleTask.class.isAssignableFrom(taskClass)) {
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
                            BukkitLifeCycleTask task = (BukkitLifeCycleTask) ReflectionHelper.getSingletonClassInstance(taskClass);
                            List<Class<? extends Throwable>> ignoreExceptions = Arrays.asList(annotation.ignoreExceptions());
                            List<Class<? extends Throwable>> printExceptions = Arrays.asList(annotation.printExceptions());
                            BukkitLifeCycleTaskWrapper wrapper = new BukkitLifeCycleTaskWrapper(task, priority, ignoreExceptions, printExceptions);
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
        taskWrappers.sort(Comparator.comparingInt(BukkitLifeCycleTaskWrapper::priority));
        for (BukkitLifeCycleTaskWrapper taskWrapper : taskWrappers) {
            taskWrapper.runLifecycleTask(this, lifeCycle);
        }
    }

}
