package crypticlib.config;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import crypticlib.VelocityPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public class VelocityConfigWrapper extends ConfigWrapper<FileConfig> {

    public VelocityConfigWrapper(@NotNull VelocityPlugin plugin, @NotNull String path) {
        super(plugin.dataFolder(), path);
    }

    public VelocityConfigWrapper(@NotNull File file) {
        super(file);
    }

    @Override
    public boolean contains(String key) {
        return config.contains(key);
    }

    @Override
    public void set(@NotNull String key, @Nullable Object object) {
        synchronized (this) {
            config.set(key, object);
        }
    }

    @Override
    public void setComments(@NotNull String key, @Nullable List<String> comments) {
        //Velocity的Config不支持注解
    }

    @Override
    public @Nullable List<String> getComments(@NotNull String key) {
        //Velocity的Config不支持注解
        return null;
    }

    @Override
    public void reloadConfig() {
        saveDefaultConfigFile();
        config = FileConfig.builder(configFile).concurrent().build();
        config.load();
    }

    @Override
    public void saveConfig() {
        synchronized (this) {
            config.save();
        }
    }

}