package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

    @Override
    public void load(@NotNull ConfigurationSection config) {
        setValue(config.getLongList(key));
        setComments(getCommentsFromConfig());
    }

}
