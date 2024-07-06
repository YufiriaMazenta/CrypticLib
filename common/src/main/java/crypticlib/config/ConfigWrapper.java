package crypticlib.config;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import crypticlib.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

/**
 * 对配置文件的封装，支持yaml，hocon，toml和json
 */
public class ConfigWrapper {

    public static File dataFolder;
    private final File configFile;
    private final String path;
    private FileConfig config;
    private boolean autoReload;

    /**
     * 从指定插件中释放并创建一个配置文件
     *
     * @param path   相对插件文件夹的路径
     */
    public ConfigWrapper(@NotNull String path) {
        this.path = path;
        this.configFile = new File(dataFolder, path);
        loadConfig();
    }

    /**
     * 从指定的File对象中创建一个配置文件
     *
     * @param file 创建的配置文件
     */
    public ConfigWrapper(@NotNull File file) {
        this.configFile = file;
        this.path = file.getPath();
        if (!configFile.exists()) {
            FileUtil.createNewFile(file);
        }
        loadConfig();
    }

    /**
     * 获取配置文件实例
     *
     * @return 配置文件实例
     */
    @NotNull
    public FileConfig config() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    public boolean contains(String key) {
        return config.contains(key);
    }

    /**
     * 设置配置文件指定路径的值
     *
     * @param key    配置的路径
     * @param object 值
     */
    public void set(@NotNull String key, @Nullable Object object) {
        config.set(key, object);
    }

    /**
     * 重载配置文件
     */
    public void reloadConfig() {
        config = FileConfig.of(configFile);
    }

    /**
     * 保存配置文件
     */
    public synchronized void saveConfig() {
        config.save();
    }

    /**
     * 返回配置文件相对于插件文件夹的路径
     *
     * @return 配置文件的路径
     */
    @NotNull
    public String filePath() {
        return path;
    }

    @NotNull
    public File configFile() {
        return configFile;
    }

    private void loadConfig() {
        try {
            this.config = FileConfig.builder(configFile).defaultResource(path).build();
        } catch (Throwable e) {
            this.config = FileConfig.builder(configFile).onFileNotFound(FileNotFoundAction.CREATE_EMPTY).build();
        }

    }

}
