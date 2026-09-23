package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class StringConfig extends BukkitConfigNode<String> {

    public StringConfig(@NotNull String key, @NotNull String def) {
        super(key, def);
    }

    public StringConfig(String key, String def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public StringConfig(@NotNull String key, @NotNull String def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public StringConfig(@NotNull String key, @NotNull Supplier<String> defFactory) {
        super(key, defFactory);
    }

    public StringConfig(@NotNull String key, @NotNull Supplier<String> defFactory, @NotNull String defComment) {
        super(key, defFactory, defComment);
    }

    public StringConfig(@NotNull String key, @NotNull Supplier<String> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    protected String readValue(@NotNull ConfigurationSection config) {
        return config.isString(key) ? config.getString(key, def) : null;
    }

}
