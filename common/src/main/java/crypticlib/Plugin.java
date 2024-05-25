package crypticlib;

import crypticlib.internal.Platform;
import crypticlib.internal.reflect.PluginScanner;
import crypticlib.internal.reflect.ReflectUtil;

import java.io.File;
import java.util.logging.Logger;

public abstract class Plugin {

    private File pluginFile;
    private File dataFolder;
    private Logger logger;
    private ClassLoader classLoader;

    public void onLoad() {}

    public void onEnable() {}

    public void onDisable() {}

    public static Plugin findImpl(Platform platform) {
        Class<? extends Plugin> implClass = PluginScanner.INSTANCE.findPluginImplClass(platform);
        return ReflectUtil.getSingletonClassInstance(implClass);
    }

    public File getPluginFile() {
        return pluginFile;
    }

    public Plugin setPluginFile(File pluginFile) {
        this.pluginFile = pluginFile;
        return this;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public Plugin setDataFolder(File dataFolder) {
        this.dataFolder = dataFolder;
        return this;
    }

    public Logger getLogger() {
        return logger;
    }

    public Plugin setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Plugin setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

}
