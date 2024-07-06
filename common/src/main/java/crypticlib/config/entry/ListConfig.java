package crypticlib.config.entry;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ListConfig extends ConfigNode<List<?>> {

    public ListConfig(@NotNull String key, @NotNull List<?> def) {
        super(key, def);
    }

}
