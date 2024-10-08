package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DoubleConfig extends BukkitConfigNode<Double> {

    public DoubleConfig(@NotNull String key, @NotNull Double def) {
        super(key, def);
    }

    public DoubleConfig(String key, Double def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public DoubleConfig(@NotNull String key, @NotNull Double def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        setValue(config.getDouble(key));
        setComments(getCommentsFromConfig());
    }

}
