package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

public class BooleanConfig extends BungeeConfigNode<Boolean> {

    public BooleanConfig(@NotNull String key, @NotNull Boolean def) {
        super(key, def);
    }

    @Override
    protected Boolean readValue(@NotNull Configuration config) {
        Object raw = config.get(key);
        return raw instanceof Boolean ? (Boolean) raw : null;
    }

}
