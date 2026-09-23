package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BooleanListConfig extends VelocityConfigNode<List<Boolean>> {

    public BooleanListConfig(@NotNull String key, @NotNull List<Boolean> def) {
        super(key, def);
    }

    public BooleanListConfig(@NotNull String key, @NotNull List<Boolean> def, @NotNull String comment) {
        super(key, def, comment);
    }

    public BooleanListConfig(@NotNull String key, @NotNull List<Boolean> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public BooleanListConfig(@NotNull String key, @NotNull Supplier<List<Boolean>> defFactory) {
        super(key, defFactory);
    }

    public BooleanListConfig(@NotNull String key, @NotNull Supplier<List<Boolean>> defFactory, @NotNull String comment) {
        super(key, defFactory, comment);
    }

    public BooleanListConfig(@NotNull String key, @NotNull Supplier<List<Boolean>> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    protected List<Boolean> readValue(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        if (!(raw instanceof List)) {
            return null;
        }
        List<Boolean> result = new ArrayList<>();
        for (Object element : (List<?>) raw) {
            if (element instanceof Boolean) {
                result.add((Boolean) element);
            }
        }
        return result;
    }

}
