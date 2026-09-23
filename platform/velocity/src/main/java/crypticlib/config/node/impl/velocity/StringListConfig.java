package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class StringListConfig extends VelocityConfigNode<List<String>> {

    public StringListConfig(@NotNull String key, @NotNull List<String> def) {
        super(key, def);
    }

    public StringListConfig(@NotNull String key, @NotNull List<String> def, @NotNull String comment) {
        super(key, def, comment);
    }

    public StringListConfig(@NotNull String key, @NotNull List<String> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public StringListConfig(@NotNull String key, @NotNull Supplier<List<String>> defFactory) {
        super(key, defFactory);
    }

    public StringListConfig(@NotNull String key, @NotNull Supplier<List<String>> defFactory, @NotNull String comment) {
        super(key, defFactory, comment);
    }

    public StringListConfig(@NotNull String key, @NotNull Supplier<List<String>> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    protected List<String> readValue(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        if (!(raw instanceof List)) {
            return null;
        }
        List<String> result = new ArrayList<>();
        for (Object element : (List<?>) raw) {
            if (element instanceof String) {
                result.add((String) element);
            }
        }
        return result;
    }

}
