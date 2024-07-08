package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShortListConfig extends BungeeConfigNode<List<Short>> {

    public ShortListConfig(@NotNull String key, @NotNull List<Short> def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull Configuration config) {
        saveDef(config);
        setValue(config.getShortList(key));
    }

}
