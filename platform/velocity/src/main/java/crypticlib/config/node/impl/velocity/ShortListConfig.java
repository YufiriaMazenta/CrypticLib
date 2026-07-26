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
    public void load(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        if (raw instanceof List) {
            List<Short> value = new ArrayList<>();
            for (Object element : (List<?>) raw) {
                if (element instanceof Number) {
                    value.add(((Number) element).shortValue());
                }
            }
            setValue(value);
        } else {
            setValue(def);
        }
        setComments(configContainer.configWrapper().getComments(key));
    }

}
