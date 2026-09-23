package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

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

    public IntConfig(@NotNull String key, @NotNull Supplier<Integer> defFactory) {
        super(key, defFactory);
    }

    public IntConfig(@NotNull String key, @NotNull Supplier<Integer> defFactory, @NotNull String comment) {
        super(key, defFactory, comment);
    }

    public IntConfig(@NotNull String key, @NotNull Supplier<Integer> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    protected Integer readValue(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        return raw instanceof Number ? ((Number) raw).intValue() : null;
    }

}
