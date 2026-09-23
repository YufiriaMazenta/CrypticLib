package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class BooleanConfig extends BukkitConfigNode<Boolean> {

    public BooleanConfig(@NotNull String key, @NotNull Boolean def) {
        super(key, def);
    }

    public BooleanConfig(String key, Boolean def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public BooleanConfig(@NotNull String key, @NotNull Boolean def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public BooleanConfig(@NotNull String key, @NotNull Supplier<Boolean> defFactory) {
        super(key, defFactory);
    }

    public BooleanConfig(@NotNull String key, @NotNull Supplier<Boolean> defFactory, @NotNull String defComment) {
        super(key, defFactory, defComment);
    }

    public BooleanConfig(@NotNull String key, @NotNull Supplier<Boolean> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    protected Boolean readValue(@NotNull ConfigurationSection config) {
        return config.isBoolean(key) ? config.getBoolean(key) : null;
    }

}
