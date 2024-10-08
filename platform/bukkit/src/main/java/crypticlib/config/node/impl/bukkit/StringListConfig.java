package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

    @Override
    public void load(@NotNull ConfigurationSection config) {
        setValue(config.getStringList(key));
        setComments(getCommentsFromConfig());
    }

}
