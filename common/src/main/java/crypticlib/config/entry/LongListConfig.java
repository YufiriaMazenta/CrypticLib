package crypticlib.config.entry;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LongListConfig extends ConfigNode<List<Long>> {

    public LongListConfig(@NotNull String key, @NotNull List<Long> def) {
        super(key, def);
    }

}
