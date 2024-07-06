package crypticlib.config.entry;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IntListConfig extends ConfigNode<List<Integer>> {

    public IntListConfig(@NotNull String key, @NotNull List<Integer> def) {
        super(key, def);
    }

}
