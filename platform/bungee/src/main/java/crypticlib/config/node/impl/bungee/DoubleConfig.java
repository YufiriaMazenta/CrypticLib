package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

public class DoubleConfig extends BungeeConfigNode<Double> {

    public DoubleConfig(@NotNull String key, @NotNull Double def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull Configuration config) {
        saveDef(config);
        setValue(config.getDouble(key));
    }

}
