package crypticlib.config.entry;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DoubleListConfig extends ConfigNode<List<Double>> {

    public DoubleListConfig(@NotNull String key, @NotNull List<Double> def) {
        super(key, def);
    }

}
