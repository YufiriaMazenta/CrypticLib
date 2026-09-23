package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DoubleListConfig extends VelocityConfigNode<List<Double>> {

    public DoubleListConfig(@NotNull String key, @NotNull List<Double> def) {
        super(key, def);
    }

    public DoubleListConfig(@NotNull String key, @NotNull List<Double> def, @NotNull String comment) {
        super(key, def, comment);
    }

    public DoubleListConfig(@NotNull String key, @NotNull List<Double> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    protected List<Double> readValue(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        if (!(raw instanceof List)) {
            return null;
        }
        List<Double> result = new ArrayList<>();
        for (Object element : (List<?>) raw) {
            if (element instanceof Number) {
                result.add(((Number) element).doubleValue());
            }
        }
        return result;
    }

}
