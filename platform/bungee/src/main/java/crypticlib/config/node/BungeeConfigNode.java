package crypticlib.config.node;

import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

public abstract class BungeeConfigNode<T> extends ConfigNode<T, Configuration> {

    public BungeeConfigNode(@NotNull String key, @NotNull T def) {
        super(key, def);
    }

    @Override
    public void saveDef(@NotNull Configuration config) {
        //加载默认值
        if (!config.contains(key)) {
            setValue(def);
        }
        load(config);
    }

}
