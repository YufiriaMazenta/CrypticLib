package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

public class IntConfig extends BungeeConfigNode<Integer> {

    public IntConfig(@NotNull String key, @NotNull Integer def) {
        super(key, def);
    }

    @Override
    protected Integer readValue(@NotNull Configuration config) {
        Object raw = config.get(key);
        return raw instanceof Number ? ((Number) raw).intValue() : null;
    }

}
