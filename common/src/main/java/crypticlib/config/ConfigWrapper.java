package crypticlib.config;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import crypticlib.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import java.util.Scanner;

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
        saveDefaultConfigFile();
        reloadConfig();
    }

    /**
     * 从指定的File对象中创建一个配置文件
     *
     * @param file 创建的配置文件
     */
    public ConfigWrapper(@NotNull File file) {
        this.configFile = file;
        this.path = file.getPath();
        saveDefaultConfigFile();
        reloadConfig();
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
        if (!configFile.exists()) {
            saveDefaultConfigFile();
        }
        this.config = FileConfig.of(configFile);
        this.config.load();
    }

    /**
     * 保存配置文件
     */
    public synchronized void saveConfig() {
        config.save();
    }

    public void saveDefaultConfigFile() {
        if (!configFile.exists()) {
            try {
                if (!dataFolder.exists()) {
                    dataFolder.mkdirs();
                }
                if (!configFile.exists()) {
                    FileOutputStream output = new FileOutputStream(configFile);
                    InputStream input = getResource(path);
                    if (input == null)
                        throw new NullPointerException();
                    Objects.requireNonNull(output, "out");
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = input.read(buffer, 0, 8192)) >= 0) {
                        output.write(buffer, 0, read);
                    }
                    output.close();
                    input.close();
                }
            } catch (NullPointerException | IllegalArgumentException | IOException e) {
                e.printStackTrace();
                FileUtil.createNewFile(configFile);
            }
        }
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

    private @Nullable InputStream getResource(@NotNull String filename) {
        try {
            URL url = this.getClass().getClassLoader().getResource(filename);
            if (url == null) {
                return null;
            } else {
                URLConnection connection = url.openConnection();
                connection.setUseCaches(false);
                return connection.getInputStream();
            }
        } catch (IOException var4) {
            return null;
        }
    }

}
