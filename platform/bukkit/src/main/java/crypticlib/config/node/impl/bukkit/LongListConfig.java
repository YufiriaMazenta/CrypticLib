package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class LongListConfig extends BukkitConfigNode<List<Long>> {

    public LongListConfig(@NotNull String key, @NotNull List<Long> def) {
        super(key, def);
    }

    public LongListConfig(String key, List<Long> def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public LongListConfig(@NotNull String key, @NotNull List<Long> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public LongListConfig(@NotNull String key, @NotNull Supplier<List<Long>> defFactory) {
        super(key, defFactory);
    }

    public LongListConfig(@NotNull String key, @NotNull Supplier<List<Long>> defFactory, @NotNull String defComment) {
        super(key, defFactory, defComment);
    }

    public LongListConfig(@NotNull String key, @NotNull Supplier<List<Long>> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    protected List<Long> readValue(@NotNull ConfigurationSection config) {
        return config.isList(key) ? config.getLongList(key) : null;
    }

}
