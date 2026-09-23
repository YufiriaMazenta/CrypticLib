package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class LongConfig extends BukkitConfigNode<Long> {

    public LongConfig(@NotNull String key, @NotNull Long def) {
        super(key, def);
    }

    public LongConfig(String key, Long def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public LongConfig(@NotNull String key, @NotNull Long def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public LongConfig(@NotNull String key, @NotNull Supplier<Long> defFactory) {
        super(key, defFactory);
    }

    public LongConfig(@NotNull String key, @NotNull Supplier<Long> defFactory, @NotNull String defComment) {
        super(key, defFactory, defComment);
    }

    public LongConfig(@NotNull String key, @NotNull Supplier<Long> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    protected Long readValue(@NotNull ConfigurationSection config) {
        Object raw = config.get(key);
        return raw instanceof Number ? ((Number) raw).longValue() : null;
    }

}
