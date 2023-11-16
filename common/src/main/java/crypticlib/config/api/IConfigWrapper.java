package crypticlib.config.api;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface IConfigWrapper<T> {
    /**
     * 创建默认配置文件
     */
    void createDefaultConfig();

    T config();

    void set(String key, Object object);

    void reloadConfig();

    void saveConfig();

    boolean isAutoSave();

    void setAutoSave(boolean autoSave);

    boolean isAutoReload();

    void setAutoReload(boolean autoReload);

    String filePath();

    File configFile();

    @Nullable Plugin plugin();

}
