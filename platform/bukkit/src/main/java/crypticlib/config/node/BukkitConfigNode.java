package crypticlib.config.node;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public abstract class BukkitConfigNode<T> extends ConfigNode<T, ConfigurationSection> {

    public BukkitConfigNode(@NotNull String key, @NotNull T def) {
        super(key, def);
    }

    @Override
    public void saveDef(@NotNull ConfigurationSection config) {
        if (!config.contains(key)) {
            config.set(key, def);
        }
    }

}
