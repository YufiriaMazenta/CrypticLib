package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

    @SuppressWarnings("unchecked")
    @Override
    protected List<T> readValue(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        return raw instanceof List ? (List<T>) raw : null;
    }

}
