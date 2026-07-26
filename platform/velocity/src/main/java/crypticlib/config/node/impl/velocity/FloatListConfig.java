package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public void load(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        if (raw instanceof List) {
            List<Float> value = new ArrayList<>();
            for (Object element : (List<?>) raw) {
                if (element instanceof Number) {
                    value.add(((Number) element).floatValue());
                }
            }
            setValue(value);
        } else {
            setValue(def);
        }
        setComments(configContainer.configWrapper().getComments(key));
    }

}
