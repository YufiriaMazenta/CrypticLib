package crypticlib.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FormatDetector;
import crypticlib.VelocityPlugin;
import crypticlib.internal.config.yaml.YamlFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VelocityConfigWrapper extends ConfigWrapper<CommentedFileConfig> {

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
        synchronized (lock) {
            config.set(key, object);
        }
    }

    @Override
    public void setComments(@NotNull String key, @Nullable List<String> comments) {
        if (comments == null || comments.isEmpty())
            return;
        config.setComment(key, comments.get(0));
    }

    @Override
    public @Nullable List<String> getComments(@NotNull String key) {
        String comment = config.getComment(key);
        if (comment == null)
            return null;
        else
            return new ArrayList<>(Collections.singletonList(comment));
    }

    @Override
    public void reloadConfig() {
        saveDefaultConfigFile();
        FormatDetector.registerExtension("yaml", YamlFormat.defaultInstance());
        FormatDetector.registerExtension("yml", YamlFormat.defaultInstance());
        config = CommentedFileConfig.ofConcurrent(configFile);
        config.load();
    }

    @Override
    public void saveConfig() {
        synchronized (lock) {
            FormatDetector.registerExtension("yaml", YamlFormat.defaultInstance());
            FormatDetector.registerExtension("yml", YamlFormat.defaultInstance());
            config.save();
        }
    }

}
