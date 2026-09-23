package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class FloatListConfig extends VelocityConfigNode<List<Float>> {

    public FloatListConfig(@NotNull String key, @NotNull List<Float> def) {
        super(key, def);
    }

    public FloatListConfig(@NotNull String key, @NotNull List<Float> def, @NotNull String comment) {
        super(key, def, comment);
    }

    public FloatListConfig(@NotNull String key, @NotNull List<Float> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public FloatListConfig(@NotNull String key, @NotNull Supplier<List<Float>> defFactory) {
        super(key, defFactory);
    }

    public FloatListConfig(@NotNull String key, @NotNull Supplier<List<Float>> defFactory, @NotNull String comment) {
        super(key, defFactory, comment);
    }

    public FloatListConfig(@NotNull String key, @NotNull Supplier<List<Float>> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    protected List<Float> readValue(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        if (!(raw instanceof List)) {
            return null;
        }
        List<Float> result = new ArrayList<>();
        for (Object element : (List<?>) raw) {
            if (element instanceof Number) {
                result.add(((Number) element).floatValue());
            }
        }
        return result;
    }

}
