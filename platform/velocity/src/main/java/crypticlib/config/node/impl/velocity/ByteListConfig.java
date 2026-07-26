package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ByteListConfig extends VelocityConfigNode<List<Byte>> {

    public ByteListConfig(@NotNull String key, @NotNull List<Byte> def) {
        super(key, def);
    }

    public ByteListConfig(@NotNull String key, @NotNull List<Byte> def, @NotNull String comment) {
        super(key, def, comment);
    }

    public ByteListConfig(@NotNull String key, @NotNull List<Byte> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    public void load(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        if (raw instanceof List) {
            List<Byte> value = new ArrayList<>();
            for (Object element : (List<?>) raw) {
                if (element instanceof Number) {
                    value.add(((Number) element).byteValue());
                }
            }
            setValue(value);
        } else {
            setValue(def);
        }
        setComments(configContainer.configWrapper().getComments(key));
    }

}
