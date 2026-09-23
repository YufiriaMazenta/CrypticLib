package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class ShortListConfig extends BukkitConfigNode<List<Short>> {

    public ShortListConfig(@NotNull String key, @NotNull List<Short> def) {
        super(key, def);
    }

    public ShortListConfig(String key, List<Short> def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public ShortListConfig(@NotNull String key, @NotNull List<Short> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public ShortListConfig(@NotNull String key, @NotNull Supplier<List<Short>> defFactory) {
        super(key, defFactory);
    }

    public ShortListConfig(@NotNull String key, @NotNull Supplier<List<Short>> defFactory, @NotNull String defComment) {
        super(key, defFactory, defComment);
    }

    public ShortListConfig(@NotNull String key, @NotNull Supplier<List<Short>> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    protected List<Short> readValue(@NotNull ConfigurationSection config) {
        return config.isList(key) ? config.getShortList(key) : null;
    }

}
