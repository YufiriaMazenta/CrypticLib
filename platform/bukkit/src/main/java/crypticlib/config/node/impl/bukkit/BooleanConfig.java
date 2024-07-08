package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BooleanConfig extends BukkitConfigNode<Boolean> {

    public BooleanConfig(@NotNull String key, @NotNull Boolean def) {
        super(key, def);
    }

    public BooleanConfig(@NotNull String key, @NotNull Boolean def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getBoolean(key));
        setComments(getCommentsFromConfig());
    }

}
