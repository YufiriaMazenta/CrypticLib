package crypticlib.config.node.impl;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BooleanListConfig extends BukkitConfigNode<List<Boolean>> {

    public BooleanListConfig(@NotNull String key, @NotNull List<Boolean> def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getBooleanList(key));
        setComments(config.getComments(key));
    }

}
