package crypticlib.config.entry;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShortListConfig extends ConfigNode<List<Short>> {

    public ShortListConfig(@NotNull String key, @NotNull List<Short> def) {
        super(key, def);
    }

}
