package crypticlib.config.node.impl;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DoubleListConfig extends BukkitConfigNode<List<Double>> {

    public DoubleListConfig(@NotNull String key, @NotNull List<Double> def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getDoubleList(key));
        setComments(config.getComments(key));
    }

}
