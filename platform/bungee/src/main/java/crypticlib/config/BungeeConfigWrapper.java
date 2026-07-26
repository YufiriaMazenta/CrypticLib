package crypticlib.config;

import crypticlib.util.IOHelper;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.JsonConfiguration;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class BungeeConfigWrapper extends ConfigWrapper<Configuration> {

    public BungeeConfigWrapper(@NotNull Plugin plugin, @NotNull String path) {
        super(plugin.getDataFolder(), path);
    }

    public BungeeConfigWrapper(@NotNull File file) {
        super(file);
    }

    @Override
    public boolean contains(String key) {
        return config().contains(key);
    }

    @Override
    public void set(@NotNull String key, @Nullable Object object) {
        synchronized (lock) {
            config().set(key, object);
        }
    }

    @Override
    public void setComments(@NotNull String key, @Nullable List<String> comments) {
        //Bungee的Config不支持注解
    }

    @Override
    public @Nullable List<String> getComments(@NotNull String key) {
        //Bungee的Config不支持注解
        return null;
    }

    @Override
    public void reloadConfig() {
        synchronized (lock) {
            saveDefaultConfigFile();
            ConfigurationProvider provider;
            if (IOHelper.isYamlFile(configFile)) {
                provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
            } else if (IOHelper.isJsonFile(configFile)) {
                //json配置只在1.15以上可用
                provider = ConfigurationProvider.getProvider(JsonConfiguration.class);
            } else {
                throw new IllegalArgumentException("Unsupported file type: " + path);
            }
            Configuration newConfig;
            try {
                newConfig = provider.load(configFile);
            } catch (FileNotFoundException e) {
                //文件不存在时保持空配置
                newConfig = new Configuration();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } catch (RuntimeException e) {
                //解析失败: 保留用户原文件并另存.broken备份, 中止本次重载,
                //绝不能吞掉异常后以空配置继续, 否则后续saveConfig会把用户配置覆盖掉
                backupBrokenConfigFile();
                throw new IllegalStateException(
                    "Failed to parse config file " + configFile
                        + ", the original file has been kept and a backup was saved as "
                        + configFile.getName() + ".broken", e);
            }
            config = newConfig;
        }
    }

    private void backupBrokenConfigFile() {
        try {
            File broken = new File(configFile.getAbsolutePath() + ".broken");
            Files.copy(configFile.toPath(), broken.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void saveConfig() {
        synchronized (lock) {
            try {
                if (IOHelper.isYamlFile(configFile)) {
                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(config(), configFile);
                } else if (IOHelper.isJsonFile(configFile)) {
                    ConfigurationProvider.getProvider(JsonConfiguration.class).save(config(), configFile);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
