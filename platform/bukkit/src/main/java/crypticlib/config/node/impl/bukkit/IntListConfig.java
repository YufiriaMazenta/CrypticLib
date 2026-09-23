package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class IntListConfig extends BukkitConfigNode<List<Integer>> {

    public IntListConfig(@NotNull String key, @NotNull List<Integer> def) {
        super(key, def);
    }

    public IntListConfig(String key, List<Integer> def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public IntListConfig(@NotNull String key, @NotNull List<Integer> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public IntListConfig(@NotNull String key, @NotNull Supplier<List<Integer>> defFactory) {
        super(key, defFactory);
    }

    public IntListConfig(@NotNull String key, @NotNull Supplier<List<Integer>> defFactory, @NotNull String defComment) {
        super(key, defFactory, defComment);
    }

    public IntListConfig(@NotNull String key, @NotNull Supplier<List<Integer>> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    protected List<Integer> readValue(@NotNull ConfigurationSection config) {
        return config.isList(key) ? config.getIntegerList(key) : null;
    }

}
