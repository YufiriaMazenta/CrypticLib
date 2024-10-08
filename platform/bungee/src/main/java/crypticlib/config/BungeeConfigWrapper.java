package crypticlib.config;

import crypticlib.util.FileHelper;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.JsonConfiguration;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
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
        return config.contains(key);
    }

    @Override
    public void set(@NotNull String key, @Nullable Object object) {
        synchronized (lock) {
            config.set(key, object);
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
        saveDefaultConfigFile();
        try {
            if (FileHelper.isYamlFile(path)) {
                config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            } else if (FileHelper.isJsonFile(path)) {
                //json配置只在1.15以上可用
                config = ConfigurationProvider.getProvider(JsonConfiguration.class).load(configFile);
            } else {
                throw new IllegalArgumentException("Unsupported file type: " + path);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveConfig() {
        synchronized (lock) {
            try {
                if (FileHelper.isYamlFile(path)) {
                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, configFile);
                } else if (FileHelper.isJsonFile(path)) {
                    ConfigurationProvider.getProvider(JsonConfiguration.class).save(config, configFile);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
