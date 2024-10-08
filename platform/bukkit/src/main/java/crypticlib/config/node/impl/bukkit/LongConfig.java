package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

    @Override
    public void load(@NotNull ConfigurationSection config) {
        setValue(config.getLong(key));
        setComments(getCommentsFromConfig());
    }

}
