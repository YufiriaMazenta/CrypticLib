package crypticlib.config;

import crypticlib.MinecraftVersion;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class BukkitConfigWrapper extends ConfigWrapper<YamlConfiguration> {

    public BukkitConfigWrapper(@NotNull Plugin plugin, @NotNull String path) {
        super(plugin.getDataFolder(), path);
    }

    public BukkitConfigWrapper(@NotNull File file) {
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
        synchronized (lock) {
            if (MinecraftVersion.current().before(MinecraftVersion.V1_18_1)) {
                //1.18.1以下不支持注释
                return;
            }
            config().setComments(key, comments);
        }
    }

    @Override
    public @Nullable List<String> getComments(@NotNull String key) {
        if (MinecraftVersion.current().afterOrEquals(MinecraftVersion.V1_18_1)) {
            return config().getComments(key);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void reloadConfig() {
        synchronized (lock) {
            saveDefaultConfigFile();
            YamlConfiguration newConfig = new YamlConfiguration();
            try {
                newConfig.load(configFile);
            } catch (InvalidConfigurationException e) {
                //YAML解析失败: 保留用户原文件并另存.broken备份, 中止本次重载,
                //绝不能以空配置为基础在后续saveConfig时把用户的配置和注释覆盖掉
                backupBrokenConfigFile();
                throw new IllegalStateException(
                    "Failed to parse config file " + configFile
                        + ", the original file has been kept and a backup was saved as "
                        + configFile.getName() + ".broken", e);
            } catch (FileNotFoundException e) {
                //文件不存在时保持空配置
            } catch (IOException e) {
                e.printStackTrace();
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
                config().save(configFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
