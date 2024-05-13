package crypticlib;

import crypticlib.annotation.PluginScanner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public abstract class CrypticLibPlugin {

    private @Nullable File pluginFile;

    abstract void onLoad();

    abstract void onEnable();

    abstract void onDisable();

    public final void loadPlugin(File pluginFile) {
        setPluginFile(pluginFile);
        PluginScanner.INSTANCE.scanJar(pluginFile);
        onLoad();
    }

    public @Nullable File getPluginFile() {
        return pluginFile;
    }

    public CrypticLibPlugin setPluginFile(File pluginFile) {
        this.pluginFile = pluginFile;
        return this;
    }

}
