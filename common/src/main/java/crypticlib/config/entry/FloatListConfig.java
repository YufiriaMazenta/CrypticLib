package crypticlib.config.entry;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FloatListConfig extends ConfigNode<List<Float>> {

    public FloatListConfig(@NotNull String key, @NotNull List<Float> def) {
        super(key, def);
    }

}
