package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class DoubleListConfig extends BukkitConfigNode<List<Double>> {

    public DoubleListConfig(@NotNull String key, @NotNull List<Double> def) {
        super(key, def);
    }

    public DoubleListConfig(String key, List<Double> def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public DoubleListConfig(@NotNull String key, @NotNull List<Double> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public DoubleListConfig(@NotNull String key, @NotNull Supplier<List<Double>> defFactory) {
        super(key, defFactory);
    }

    public DoubleListConfig(@NotNull String key, @NotNull Supplier<List<Double>> defFactory, @NotNull String defComment) {
        super(key, defFactory, defComment);
    }

    public DoubleListConfig(@NotNull String key, @NotNull Supplier<List<Double>> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    protected List<Double> readValue(@NotNull ConfigurationSection config) {
        return config.isList(key) ? config.getDoubleList(key) : null;
    }

}
