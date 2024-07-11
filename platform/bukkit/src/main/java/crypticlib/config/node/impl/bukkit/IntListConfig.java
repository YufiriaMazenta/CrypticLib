package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IntListConfig extends BukkitConfigNode<List<Integer>> {

    public IntListConfig(@NotNull String key, @NotNull List<Integer> def) {
        super(key, def);
    }

    public IntListConfig(String key, List<Integer> def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public IntListConfig(@NotNull String key, @NotNull List<Integer> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getIntegerList(key));
        setComments(getCommentsFromConfig());
    }

}
