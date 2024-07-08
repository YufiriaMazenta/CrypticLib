package crypticlib.config.node;

import crypticlib.MinecraftVersion;
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

    @Override
    public void saveDef(@NotNull ConfigurationSection config) {
        //加载默认值
        if (!config.contains(key)) {
            config.set(key, def);
        }

        //加载默认注释
        if (MinecraftVersion.current().afterOrEquals(MinecraftVersion.V1_18_1)) {
            if (defComments == null) {
                return;
            }
            if (!config.getComments(key).isEmpty()) {
                return;
            }
            config.setComments(key, defComments);
        }
    }

    protected List<String> getCommentsFromConfig() {
        return configContainer.configWrapper().getComments(key);
    }

}
