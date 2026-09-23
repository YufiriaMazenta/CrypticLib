package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

public class StringConfig extends BungeeConfigNode<String> {

    public StringConfig(@NotNull String key, @NotNull String def) {
        super(key, def);
    }

    @Override
    protected String readValue(@NotNull Configuration config) {
        Object raw = config.get(key);
        return raw instanceof CharSequence ? raw.toString() : null;
    }

}
