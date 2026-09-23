package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class StringListConfig extends BukkitConfigNode<List<String>> {

    public StringListConfig(@NotNull String key, @NotNull List<String> def) {
        super(key, def);
    }

    public StringListConfig(String key, List<String> def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public StringListConfig(@NotNull String key, @NotNull List<String> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public StringListConfig(@NotNull String key, @NotNull Supplier<List<String>> defFactory) {
        super(key, defFactory);
    }

    public StringListConfig(@NotNull String key, @NotNull Supplier<List<String>> defFactory, @NotNull String defComment) {
        super(key, defFactory, defComment);
    }

    public StringListConfig(@NotNull String key, @NotNull Supplier<List<String>> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    protected List<String> readValue(@NotNull ConfigurationSection config) {
        return config.isList(key) ? config.getStringList(key) : null;
    }

}
