package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

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

    public StringConfig(@NotNull String key, @NotNull Supplier<String> defFactory) {
        super(key, defFactory);
    }

    public StringConfig(@NotNull String key, @NotNull Supplier<String> defFactory, @NotNull String comment) {
        super(key, defFactory, comment);
    }

    public StringConfig(@NotNull String key, @NotNull Supplier<String> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    protected String readValue(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        return raw instanceof String ? (String) raw : null;
    }

}
