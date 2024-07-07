package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class IntConfig extends BukkitConfigNode<Integer> {

    public IntConfig(@NotNull String key, @NotNull Integer def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getInt(key));
        setComments(config.getComments(key));
    }

}
