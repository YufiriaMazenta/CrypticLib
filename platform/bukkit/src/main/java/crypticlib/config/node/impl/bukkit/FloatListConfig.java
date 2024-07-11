package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FloatListConfig extends BukkitConfigNode<List<Float>> {

    public FloatListConfig(@NotNull String key, @NotNull List<Float> def) {
        super(key, def);
    }

    public FloatListConfig(String key, List<Float> def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public FloatListConfig(@NotNull String key, @NotNull List<Float> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getFloatList(key));
        setComments(getCommentsFromConfig());
    }

}
