package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

public class LongConfig extends VelocityConfigNode<Long> {

    public LongConfig(@NotNull String key, @NotNull Long def) {
        super(key, def);
    }

    public LongConfig(@NotNull String key, @NotNull Long def, @NotNull String comment) {
        super(key, def, comment);
    }

    @Override
    public void load(@NotNull CommentedConfig config) {
        setValue(config.getLongOrElse(key, def));
        setComment(config.getComment(key));
    }

}
