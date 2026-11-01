package crypticlib.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FormatDetector;
import com.electronwill.nightconfig.core.io.ParsingException;
import crypticlib.CrypticLib;
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
            } catch (RuntimeException e) {
                //非解析类异常(通常是读取失败): 既不能把空配置当成新配置赋值, 也不应把用户文件搬成 .broken,
                //因为文件本身可能没有损坏。保持原有 config 不变并快速失败
                CrypticLib.error("Failed to read config file: " + configFile, e);
                throw new IllegalStateException(
                    "Failed to read config file " + configFile
                        + ", the previous configuration is kept", e);
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
