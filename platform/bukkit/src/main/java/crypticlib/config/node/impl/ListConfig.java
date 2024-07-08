package crypticlib.config.node.impl;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ListConfig<T> extends BukkitConfigNode<List<T>> {

    public ListConfig(@NotNull String key, @NotNull List<T> def) {
        super(key, def);
    }

    public ListConfig(@NotNull String key, @NotNull List<T> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue((List<T>) Objects.requireNonNull(config.getList(key)));
        setComments(getCommentsFromConfig());
    }

}
