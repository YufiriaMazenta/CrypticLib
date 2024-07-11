package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShortListConfig extends BukkitConfigNode<List<Short>> {

    public ShortListConfig(@NotNull String key, @NotNull List<Short> def) {
        super(key, def);
    }

    public ShortListConfig(String key, List<Short> def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public ShortListConfig(@NotNull String key, @NotNull List<Short> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getShortList(key));
        setComments(getCommentsFromConfig());
    }

}
