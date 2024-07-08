package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

public class StringConfig extends BungeeConfigNode<String> {

    public StringConfig(@NotNull String key, @NotNull String def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull Configuration config) {
        saveDef(config);
        setValue(config.getString(key, def));
    }

}
