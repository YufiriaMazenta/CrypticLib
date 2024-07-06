package crypticlib.config.entry;

import org.jetbrains.annotations.NotNull;

public class BooleanConfig extends ConfigNode<Boolean> {

    public BooleanConfig(@NotNull String key, @NotNull Boolean def) {
        super(key, def);
    }

}
