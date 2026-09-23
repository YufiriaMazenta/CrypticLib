package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class ListConfig<T> extends VelocityConfigNode<List<T>> {

    public ListConfig(@NotNull String key, @NotNull List<T> def) {
        super(key, def);
    }

    public ListConfig(@NotNull String key, @NotNull List<T> def, @NotNull String comment) {
        super(key, def, comment);
    }

    public ListConfig(@NotNull String key, @NotNull List<T> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public ListConfig(@NotNull String key, @NotNull Supplier<List<T>> defFactory) {
        super(key, defFactory);
    }

    public ListConfig(@NotNull String key, @NotNull Supplier<List<T>> defFactory, @NotNull String comment) {
        super(key, defFactory, comment);
    }

    public ListConfig(@NotNull String key, @NotNull Supplier<List<T>> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<T> readValue(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        return raw instanceof List ? (List<T>) raw : null;
    }

}
