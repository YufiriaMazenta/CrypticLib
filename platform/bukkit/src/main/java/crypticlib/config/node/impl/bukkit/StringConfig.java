package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

    @Override
    public void load(@NotNull ConfigurationSection config) {
        setValue(config.getString(key, def));
        setComments(getCommentsFromConfig());
    }

}
