package crypticlib.config.entry;

import org.jetbrains.annotations.NotNull;

public class StringConfig extends ConfigNode<String> {

    public StringConfig(@NotNull String key, @NotNull String def) {
        super(key, def);
    }

}
