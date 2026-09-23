package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class IntListConfig extends VelocityConfigNode<List<Integer>> {

    public IntListConfig(@NotNull String key, @NotNull List<Integer> def) {
        super(key, def);
    }

    public IntListConfig(@NotNull String key, @NotNull List<Integer> def, @NotNull String comment) {
        super(key, def, comment);
    }

    public IntListConfig(@NotNull String key, @NotNull List<Integer> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    protected List<Integer> readValue(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        if (!(raw instanceof List)) {
            return null;
        }
        List<Integer> result = new ArrayList<>();
        for (Object element : (List<?>) raw) {
            if (element instanceof Number) {
                result.add(((Number) element).intValue());
            }
        }
        return result;
    }

}
