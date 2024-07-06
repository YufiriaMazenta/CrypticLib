package crypticlib.config.entry;

import org.jetbrains.annotations.NotNull;

public class LongConfig extends ConfigNode<Long> {

    public LongConfig(@NotNull String key, @NotNull Long def) {
        super(key, def);
    }

}
