package crypticlib.config.entry;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StringListConfig extends ConfigNode<List<String>> {

    public StringListConfig(@NotNull String key, @NotNull List<String> def) {
        super(key, def);
    }

}
