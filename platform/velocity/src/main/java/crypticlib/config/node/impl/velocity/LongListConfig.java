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
    public void load(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        if (raw instanceof List) {
            List<Long> value = new ArrayList<>();
            for (Object element : (List<?>) raw) {
                if (element instanceof Number) {
                    value.add(((Number) element).longValue());
                }
            }
            setValue(value);
        } else {
            setValue(def);
        }
        setComments(configContainer.configWrapper().getComments(key));
    }

}
