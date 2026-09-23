package crypticlib.config.node;

import crypticlib.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class BukkitConfigNode<T> extends ConfigNode<T, ConfigurationSection> {

    public BukkitConfigNode(@NotNull String key, @NotNull T def) {
        super(key, def);
    }

    public BukkitConfigNode(@NotNull String key, @NotNull T def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public BukkitConfigNode(String key, T def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    @Override
    public void saveDef(@NotNull ConfigurationSection config) {
        //加载默认值
        if (!config.contains(key)) {
            setValue(def);
        }

        //加载默认注释
        if (MinecraftVersion.current().afterOrEquals(MinecraftVersion.V1_18_1)) {
            if (defComments == null) {
                return;
            }
            if (!config.getComments(key).isEmpty()) {
                return;
            }
            setComments(defComments);
        }
    }

    @Override
    protected boolean hasKey(@NotNull ConfigurationSection config) {
        return config.contains(key);
    }

    @Override
    protected void warnTypeMismatch(String key) {
        Bukkit.getLogger().warning("Config value at '" + key + "' in "
            + configContainer.configWrapper().configFile().getName()
            + " has wrong type, falling back to default " + def
            + " (the original file value is kept).");
    }

    @Override
    protected List<String> readComments() {
        return configContainer.configWrapper().getComments(key);
    }

}
