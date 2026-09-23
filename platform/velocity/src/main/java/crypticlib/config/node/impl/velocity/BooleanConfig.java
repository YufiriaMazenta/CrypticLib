package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class BooleanConfig extends VelocityConfigNode<Boolean> {

    public BooleanConfig(@NotNull String key, @NotNull Boolean def) {
        super(key, def);
    }

    public BooleanConfig(@NotNull String key, @NotNull Boolean def, @NotNull String comment) {
        super(key, def, comment);
    }

    public BooleanConfig(@NotNull String key, @NotNull Boolean def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public BooleanConfig(@NotNull String key, @NotNull Supplier<Boolean> defFactory) {
        super(key, defFactory);
    }

    public BooleanConfig(@NotNull String key, @NotNull Supplier<Boolean> defFactory, @NotNull String comment) {
        super(key, defFactory, comment);
    }

    public BooleanConfig(@NotNull String key, @NotNull Supplier<Boolean> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    protected Boolean readValue(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        return raw instanceof Boolean ? (Boolean) raw : null;
    }

}
