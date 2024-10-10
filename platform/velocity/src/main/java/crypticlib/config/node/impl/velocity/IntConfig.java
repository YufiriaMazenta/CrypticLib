package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IntConfig extends VelocityConfigNode<Integer> {

    public IntConfig(@NotNull String key, @NotNull Integer def) {
        super(key, def);
    }

    public IntConfig(@NotNull String key, @NotNull Integer def, @NotNull String comment) {
        super(key, def, comment);
    }

    public IntConfig(@NotNull String key, @NotNull Integer def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    public void load(@NotNull CommentedConfig config) {
        setValue(config.getIntOrElse(key, def));
        setComments(configContainer.configWrapper().getComments(key));
    }

}
