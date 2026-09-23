package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class LongConfig extends BungeeConfigNode<Long> {

    public LongConfig(@NotNull String key, @NotNull Long def) {
        super(key, def);
    }

    public LongConfig(@NotNull String key, @NotNull Supplier<Long> defFactory) {
        super(key, defFactory);
    }

    @Override
    protected Long readValue(@NotNull Configuration config) {
        Object raw = config.get(key);
        return raw instanceof Number ? ((Number) raw).longValue() : null;
    }

}
