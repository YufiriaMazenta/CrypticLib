package crypticlib.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FormatDetector;
import com.electronwill.nightconfig.core.io.ParsingException;
import crypticlib.VelocityPlugin;
import crypticlib.internal.config.yaml.CommentLoader;
import crypticlib.internal.config.yaml.YamlFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
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
        if (comments == null || comments.isEmpty())
            return;
        config().setComment(key, CommentLoader.commentList2JsonArray(comments));
    }

    @Override
    public @Nullable List<String> getComments(@NotNull String key) {
        String commentJsonArray = config().getComment(key);
        if (commentJsonArray == null)
            return null;
        return CommentLoader.loadCommentList(commentJsonArray);
    }

    @Override
    public void reloadConfig() {
        synchronized (lock) {
            saveDefaultConfigFile();
            FormatDetector.registerExtension("yaml", YamlFormat.defaultInstance());
            FormatDetector.registerExtension("yml", YamlFormat.defaultInstance());
            CommentedFileConfig newConfig = CommentedFileConfig.ofConcurrent(configFile);
            try {
                newConfig.load();
            } catch (ParsingException e) {
                //YAML解析失败: 把原文件移走另存为.broken, 中止本次重载,
                //绝不能以空配置为基础在后续saveConfig时把用户的配置和注释覆盖掉
                backupBrokenConfigFile();
                throw new IllegalStateException(
                    "Failed to parse config file " + configFile
                        + ", the original file has been moved to "
                        + brokenConfigFile().getName(), e);
            }
            config = newConfig;
        }
    }

    @Override
    public void saveConfig() {
        synchronized (lock) {
            FormatDetector.registerExtension("yaml", YamlFormat.defaultInstance());
            FormatDetector.registerExtension("yml", YamlFormat.defaultInstance());
            config().save();
        }
    }

}
