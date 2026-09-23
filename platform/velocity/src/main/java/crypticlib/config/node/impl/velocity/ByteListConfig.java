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
    protected List<Byte> readValue(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        if (!(raw instanceof List)) {
            return null;
        }
        List<Byte> result = new ArrayList<>();
        for (Object element : (List<?>) raw) {
            if (element instanceof Number) {
                result.add(((Number) element).byteValue());
            }
        }
        return result;
    }

}
