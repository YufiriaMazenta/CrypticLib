package crypticlib.config.node;

import com.electronwill.nightconfig.core.CommentedConfig;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Logger;

public abstract class VelocityConfigNode<T> extends ConfigNode<T, CommentedConfig> {

    private static final Logger LOGGER = Logger.getLogger("Velocity");

    public VelocityConfigNode(@NotNull String key, @NotNull T def) {
        super(key, def);
    }

    public VelocityConfigNode(@NotNull String key, @NotNull T def, @NotNull String comment) {
        super(key, def, comment);
    }

    public VelocityConfigNode(@NotNull String key, @NotNull T def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    public void saveDef(@NotNull CommentedConfig config) {
        //加载默认值
        if (!config.contains(key)) {
            setValue(def);
        }
        if (!config.containsComment(key)) {
            if (defComments != null && !defComments.isEmpty()) {
                setComments(defComments);
            }
        }
    }

    @Override
    protected boolean hasKey(@NotNull CommentedConfig config) {
        return config.contains(key);
    }

    @Override
    protected void warnTypeMismatch(String key) {
        LOGGER.warning("Config value at '" + key + "' in "
            + configContainer.configWrapper().configFile().getName()
            + " has wrong type, falling back to default " + def
            + " (the original file value is kept).");
    }

}
