package crypticlib.config.node.impl;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IntConfig extends BukkitConfigNode<Integer> {

    public IntConfig(@NotNull String key, @NotNull Integer def) {
        super(key, def);
    }

    public IntConfig(@NotNull String key, @NotNull Integer def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getInt(key));
        setComments(getCommentsFromConfig());
    }

}
