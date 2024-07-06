package crypticlib.config.entry;

import org.jetbrains.annotations.NotNull;

public class IntConfig extends ConfigNode<Integer> {

    public IntConfig(@NotNull String key, @NotNull Integer def) {
        super(key, def);
    }

}
