package crypticlib.config;

import crypticlib.MinecraftVersion;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
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
        saveDefaultConfigFile();
        config = YamlConfiguration.loadConfiguration(configFile);
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
