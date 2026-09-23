package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BooleanListConfig extends BukkitConfigNode<List<Boolean>> {

    public BooleanListConfig(@NotNull String key, @NotNull List<Boolean> def) {
        super(key, def);
    }

    public BooleanListConfig(String key, List<Boolean> def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public BooleanListConfig(@NotNull String key, @NotNull List<Boolean> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    protected List<Boolean> readValue(@NotNull ConfigurationSection config) {
        return config.isList(key) ? config.getBooleanList(key) : null;
    }

}
