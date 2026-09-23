package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class DoubleConfig extends BungeeConfigNode<Double> {

    public DoubleConfig(@NotNull String key, @NotNull Double def) {
        super(key, def);
    }

    public DoubleConfig(@NotNull String key, @NotNull Supplier<Double> defFactory) {
        super(key, defFactory);
    }

    @Override
    protected Double readValue(@NotNull Configuration config) {
        Object raw = config.get(key);
        return raw instanceof Number ? ((Number) raw).doubleValue() : null;
    }

}
