package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
