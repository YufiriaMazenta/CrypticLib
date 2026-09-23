package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class IntConfig extends BukkitConfigNode<Integer> {

    public IntConfig(@NotNull String key, @NotNull Integer def) {
        super(key, def);
    }

    public IntConfig(String key, Integer def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public IntConfig(@NotNull String key, @NotNull Integer def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public IntConfig(@NotNull String key, @NotNull Supplier<Integer> defFactory) {
        super(key, defFactory);
    }

    public IntConfig(@NotNull String key, @NotNull Supplier<Integer> defFactory, @NotNull String defComment) {
        super(key, defFactory, defComment);
    }

    public IntConfig(@NotNull String key, @NotNull Supplier<Integer> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    protected Integer readValue(@NotNull ConfigurationSection config) {
        return config.isInt(key) ? config.getInt(key) : null;
    }

}
