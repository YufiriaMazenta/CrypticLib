package crypticlib.config.node;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

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
    }

    @Override
    protected boolean hasKey(@NotNull Configuration config) {
        return config.contains(key);
    }

    @Override
    protected void warnTypeMismatch(String key) {
        ProxyServer.getInstance().getLogger().warning("Config value at '" + key + "' in "
            + configContainer.configWrapper().configFile().getName()
            + " has wrong type, falling back to default " + def
            + " (the original file value is kept).");
    }

    @Override
    protected List<String> readComments() {
        // Bungee 不支持注释
        return Collections.emptyList();
    }

}
