package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class DoubleConfig extends BukkitConfigNode<Double> {

    public DoubleConfig(@NotNull String key, @NotNull Double def) {
        super(key, def);
    }

    public DoubleConfig(String key, Double def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public DoubleConfig(@NotNull String key, @NotNull Double def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public DoubleConfig(@NotNull String key, @NotNull Supplier<Double> defFactory) {
        super(key, defFactory);
    }

    public DoubleConfig(@NotNull String key, @NotNull Supplier<Double> defFactory, @NotNull String defComment) {
        super(key, defFactory, defComment);
    }

    public DoubleConfig(@NotNull String key, @NotNull Supplier<Double> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    protected Double readValue(@NotNull ConfigurationSection config) {
        Object raw = config.get(key);
        return raw instanceof Number ? ((Number) raw).doubleValue() : null;
    }

}
