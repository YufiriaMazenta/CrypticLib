package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class ListConfig<T> extends BungeeConfigNode<List<T>> {

    public ListConfig(@NotNull String key, @NotNull List<T> def) {
        super(key, def);
    }

    public ListConfig(@NotNull String key, @NotNull Supplier<List<T>> defFactory) {
        super(key, defFactory);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<T> readValue(@NotNull Configuration config) {
        Object raw = config.get(key);
        return raw instanceof List ? (List<T>) raw : null;
    }

}
