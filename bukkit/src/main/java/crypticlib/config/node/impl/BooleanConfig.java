package crypticlib.config.node.impl;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class BooleanConfig extends BukkitConfigNode<Boolean> {

    public BooleanConfig(@NotNull String key, @NotNull Boolean def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getBoolean(key));
        setComments(config.getComments(key));
    }

}
