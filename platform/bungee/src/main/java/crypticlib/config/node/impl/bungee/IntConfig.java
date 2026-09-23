package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class IntConfig extends BungeeConfigNode<Integer> {

    public IntConfig(@NotNull String key, @NotNull Integer def) {
        super(key, def);
    }

    public IntConfig(@NotNull String key, @NotNull Supplier<Integer> defFactory) {
        super(key, defFactory);
    }

    @Override
    protected Integer readValue(@NotNull Configuration config) {
        Object raw = config.get(key);
        return raw instanceof Number ? ((Number) raw).intValue() : null;
    }

}
