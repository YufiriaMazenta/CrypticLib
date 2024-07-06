package crypticlib.config.entry;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ByteListConfig extends ConfigNode<List<Byte>> {

    public ByteListConfig(@NotNull String key, @NotNull List<Byte> def) {
        super(key, def);
    }

}
