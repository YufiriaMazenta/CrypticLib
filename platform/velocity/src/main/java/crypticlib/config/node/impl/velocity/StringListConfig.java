package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public void load(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        if (raw instanceof List) {
            List<String> value = new ArrayList<>();
            for (Object element : (List<?>) raw) {
                if (element instanceof String) {
                    value.add((String) element);
                }
            }
            setValue(value);
        } else {
            setValue(def);
        }
        setComments(configContainer.configWrapper().getComments(key));
    }

}
