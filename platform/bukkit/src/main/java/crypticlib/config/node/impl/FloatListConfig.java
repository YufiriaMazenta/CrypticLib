package crypticlib.config.node.impl;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FloatListConfig extends BukkitConfigNode<List<Float>> {

    public FloatListConfig(@NotNull String key, @NotNull List<Float> def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getFloatList(key));
        setComments(config.getComments(key));
    }

}
