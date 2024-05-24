package crypticlib.internal;

import crypticlib.Plugin;

import java.io.File;
import java.util.logging.Logger;

public class PluginUtil {

    private static Plugin pluginImpl;

    public static Plugin getPluginImpl() {
        return pluginImpl;
    }

    /**
     * 初始化CrypticLib插件
     * @param pluginFile 插件文件
     * @param dataFolder 插件数据文件夹
     * @param logger 插件Logger
     * @param classLoader 插件的类加载器
     */
    public static void initPluginImpl(File pluginFile, File dataFolder, Logger logger, ClassLoader classLoader) {
        pluginImpl = Plugin.findImpl(Platform.getCurrent());
        pluginImpl.setPluginFile(pluginFile);
        pluginImpl.setDataFolder(dataFolder);
        pluginImpl.setLogger(logger);
        pluginImpl.setClassLoader(classLoader);
    }

    /**
     * 加载插件
     */
    public static void loadPluginImpl() {
        pluginImpl.onLoad();
    }

    /**
     * 启用插件
     */
    public static void enablePluginImpl() {
        if (pluginImpl != null) {
            pluginImpl.onEnable();
        }
    }

    /**
     * 禁用插件
     */
    public static void disablePluginImpl() {
        if (pluginImpl != null) {
            pluginImpl.onDisable();
        }
    }

}
