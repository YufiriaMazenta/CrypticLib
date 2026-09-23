package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LongListConfig extends VelocityConfigNode<List<Long>> {

    public LongListConfig(@NotNull String key, @NotNull List<Long> def) {
        super(key, def);
    }

    public LongListConfig(@NotNull String key, @NotNull List<Long> def, @NotNull String comment) {
        super(key, def, comment);
    }

    public LongListConfig(@NotNull String key, @NotNull List<Long> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    protected List<Long> readValue(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        if (!(raw instanceof List)) {
            return null;
        }
        List<Long> result = new ArrayList<>();
        for (Object element : (List<?>) raw) {
            if (element instanceof Number) {
                result.add(((Number) element).longValue());
            }
        }
        return result;
    }

}
