package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ShortListConfig extends VelocityConfigNode<List<Short>> {

    public ShortListConfig(@NotNull String key, @NotNull List<Short> def) {
        super(key, def);
    }

    public ShortListConfig(@NotNull String key, @NotNull List<Short> def, @NotNull String comment) {
        super(key, def, comment);
    }

    public ShortListConfig(@NotNull String key, @NotNull List<Short> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public ShortListConfig(@NotNull String key, @NotNull Supplier<List<Short>> defFactory) {
        super(key, defFactory);
    }

    public ShortListConfig(@NotNull String key, @NotNull Supplier<List<Short>> defFactory, @NotNull String comment) {
        super(key, defFactory, comment);
    }

    public ShortListConfig(@NotNull String key, @NotNull Supplier<List<Short>> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    protected List<Short> readValue(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        if (!(raw instanceof List)) {
            return null;
        }
        List<Short> result = new ArrayList<>();
        for (Object element : (List<?>) raw) {
            if (element instanceof Number) {
                result.add(((Number) element).shortValue());
            }
        }
        return result;
    }

}
