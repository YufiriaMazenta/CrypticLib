package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LongConfig extends VelocityConfigNode<Long> {

    public LongConfig(@NotNull String key, @NotNull Long def) {
        super(key, def);
    }

    public LongConfig(@NotNull String key, @NotNull Long def, @NotNull String comment) {
        super(key, def, comment);
    }

    public LongConfig(@NotNull String key, @NotNull Long def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    public void load(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        if (raw instanceof Number) {
            setValue(((Number) raw).longValue());
        } else {
            setValue(def);
        }
        setComments(configContainer.configWrapper().getComments(key));
    }

}
