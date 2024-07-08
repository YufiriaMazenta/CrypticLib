package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IntListConfig extends BungeeConfigNode<List<Integer>> {

    public IntListConfig(@NotNull String key, @NotNull List<Integer> def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull Configuration config) {
        saveDef(config);
        setValue(config.getIntList(key));
    }

}
