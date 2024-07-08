package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ListConfig<T> extends BungeeConfigNode<List<T>> {

    public ListConfig(@NotNull String key, @NotNull List<T> def) {
        super(key, def);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void load(@NotNull Configuration config) {
        saveDef(config);
        setValue((List<T>) Objects.requireNonNull(config.getList(key)));
    }

}
