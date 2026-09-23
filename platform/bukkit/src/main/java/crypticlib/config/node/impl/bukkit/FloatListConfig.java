package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class FloatListConfig extends BukkitConfigNode<List<Float>> {

    public FloatListConfig(@NotNull String key, @NotNull List<Float> def) {
        super(key, def);
    }

    public FloatListConfig(String key, List<Float> def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public FloatListConfig(@NotNull String key, @NotNull List<Float> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public FloatListConfig(@NotNull String key, @NotNull Supplier<List<Float>> defFactory) {
        super(key, defFactory);
    }

    public FloatListConfig(@NotNull String key, @NotNull Supplier<List<Float>> defFactory, @NotNull String defComment) {
        super(key, defFactory, defComment);
    }

    public FloatListConfig(@NotNull String key, @NotNull Supplier<List<Float>> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    protected List<Float> readValue(@NotNull ConfigurationSection config) {
        return config.isList(key) ? config.getFloatList(key) : null;
    }

}
