package crypticlib.config;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface IConfigWrapper<T> {
    /**
     * 创建默认配置文件
     */
    void createDefaultConfig();

    @NotNull T config();

    void set(@NotNull String key, @Nullable Object object);

    void reloadConfig();

    void saveConfig();

    @NotNull String filePath();

    @NotNull File configFile();

    @Nullable Plugin plugin();

}
