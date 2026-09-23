package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ConfigSectionConfig extends BungeeConfigNode<Configuration> {

    public ConfigSectionConfig(@NotNull String key) {
        this(key, new HashMap<>());
    }

    public ConfigSectionConfig(@NotNull String key, @NotNull Map<String, Object> def) {
        super(key, new Configuration());
        for (Map.Entry<String, Object> entry : def.entrySet()) {
            this.def.set(entry.getKey(), entry.getValue());
        }
    }

    public ConfigSectionConfig(@NotNull String key, @NotNull Supplier<Configuration> defFactory) {
        super(key, defFactory);
    }

    @Override
    protected Configuration readValue(@NotNull Configuration config) {
        Object raw = config.get(key);
        return raw instanceof Configuration ? (Configuration) raw : null;
    }

}
