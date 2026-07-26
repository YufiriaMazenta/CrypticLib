package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StringConfig extends VelocityConfigNode<String> {

    public StringConfig(@NotNull String key, @NotNull String def) {
        super(key, def);
    }

    public StringConfig(@NotNull String key, @NotNull String def, @NotNull String comment) {
        super(key, def, comment);
    }

    public StringConfig(@NotNull String key, @NotNull String def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    public void load(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        if (raw instanceof String) {
            setValue((String) raw);
        } else {
            setValue(def);
        }
        setComments(configContainer.configWrapper().getComments(key));
    }

}
