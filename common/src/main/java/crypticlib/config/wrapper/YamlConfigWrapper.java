package crypticlib.config.wrapper;

import crypticlib.util.FileUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

/**
 * 对Yaml类型的配置文件的封装
 */
public class YamlConfigWrapper implements IConfigWrapper<YamlConfiguration> {
    private final File configFile;
    private YamlConfiguration config;
    private final String path;
    private final Plugin plugin;

    /**
     * 从指定插件中释放并创建一个配置文件
     * @param plugin 创建配置文件的插件
     * @param path 相对插件文件夹的路径
     */
    public YamlConfigWrapper(Plugin plugin, String path) {
        this.path = path;
        File dataFolder = plugin.getDataFolder();
        configFile = new File(dataFolder, path);
        this.plugin = plugin;
        createDefaultConfig();
    }

    /**
     * 从指定的File对象中创建一个配置文件
     * @param file 创建的配置文件
     */
    public YamlConfigWrapper(File file) {
        this.configFile = file;
        this.path = file.getPath();
        this.config = YamlConfiguration.loadConfiguration(file);
        this.plugin = null;
    }

    @Override
    public void createDefaultConfig() {
        if (!configFile.exists()) {
            try {
                plugin.saveResource(path, false);
            } catch (NullPointerException | IllegalArgumentException e) {
                FileUtil.createNewFile(configFile);
            }
        }
        reloadConfig();
    }

    /**
     * 获取配置文件实例
     * @return 配置文件实例
     */
    @Override
    public YamlConfiguration config() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    /**
     * 设置配置文件指定路径的值
     * @param key 配置的路径
     * @param object 值
     */
    @Override
    public void set(String key, Object object) {
        config.set(key, object);
    }

    /**
     * 重载配置文件
     */
    @Override
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * 保存配置文件
     */
    @Override
    public synchronized void saveConfig() {
        try {
            config().save(configFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 返回配置文件相对于插件文件夹的路径
     * @return 配置文件的路径
     */
    @Override
    public String filePath() { return path; }

    @Override
    public File configFile() {
        return configFile;
    }

    @Override
    public Plugin plugin() {
        return plugin;
    }

}
