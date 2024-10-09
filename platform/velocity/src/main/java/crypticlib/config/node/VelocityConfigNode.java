package crypticlib.config.node;

import com.electronwill.nightconfig.core.CommentedConfig;
import org.jetbrains.annotations.NotNull;

public abstract class VelocityConfigNode<T> extends ConfigNode<T, CommentedConfig> {

    public VelocityConfigNode(@NotNull String key, @NotNull T def) {
        super(key, def);
    }

    public VelocityConfigNode(@NotNull String key, @NotNull T def, @NotNull String comment) {
        super(key, def, comment);
    }

    @Override
    public void saveDef(@NotNull CommentedConfig config) {
        //加载默认值
        if (!config.contains(key)) {
            setValue(def);
        }
        if (!config.containsComment(key)) {
            if (defComments != null && !defComments.isEmpty()) {
                setComment(defComments.get(0));
            }
        }
    }

}
