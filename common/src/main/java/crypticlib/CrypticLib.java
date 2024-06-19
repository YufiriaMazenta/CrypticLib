package crypticlib;

import crypticlib.api.IPlatformAdapter;
import crypticlib.api.command.ICommandManager;
import crypticlib.api.scheduler.IScheduler;
import crypticlib.internal.ICrypticLibLoader;
import crypticlib.internal.reflect.PluginScanner;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class CrypticLib {

    private static boolean initialized = false;
    private static IScheduler scheduler;
    private static ICommandManager commandManager;
    private static IPlatformAdapter platformAdapter;

    private static File internalDataFolder;
    private static File internalPluginFile;
    private static Object internalPluginIns;

    /**
     * 初始化CrypticLib，建议在插件onLoad阶段进行，onEnable也不是不行
     * @param loader CrypticLib加载器
     * @param pluginFile 插件的文件，用于扫描插件的类进行初始化
     * @param dataFolder 插件的数据文件夹，用于配置文件相关管理
     * @param pluginInstance 插件的实例
     */
    public static void init(ICrypticLibLoader loader, File pluginFile, File dataFolder, Object pluginInstance) {
        loader.loadCrypticLib();
        internalPluginFile = pluginFile;
        internalDataFolder = dataFolder;
        internalPluginIns = pluginInstance;
        PluginScanner.INSTANCE.scanJar(pluginFile);
        initialized = true;
    }

    public static void enablePlugin() {
        if (!initialized) {
            throw new UnsupportedOperationException("CrypticLib is not initialized! Please invoke `CrypticLib.init` method before use this method.");
        }
    }

    public static void disablePlugin() {

    }

    //以下set方法均为内部使用，一般情况下请勿调用

    public static @NotNull IScheduler getScheduler() {
        return scheduler;
    }

    @Deprecated
    public static void setScheduler(@NotNull IScheduler scheduler) {
        CrypticLib.scheduler = scheduler;
    }

    public static ICommandManager getCommandManager() {
        return commandManager;
    }

    @Deprecated
    public static void setCommandManager(ICommandManager commandManager) {
        CrypticLib.commandManager = commandManager;
    }

    public static IPlatformAdapter getPlatformAdapter() {
        return platformAdapter;
    }

    @Deprecated
    public static void setPlatformAdapter(IPlatformAdapter platformAdapter) {
        CrypticLib.platformAdapter = platformAdapter;
    }

    public static File getDataFolder() {
        return internalDataFolder;
    }

    public static File getPluginFile() {
        return internalPluginFile;
    }

    public static Object getPluginIns() {
        return internalPluginIns;
    }
}
