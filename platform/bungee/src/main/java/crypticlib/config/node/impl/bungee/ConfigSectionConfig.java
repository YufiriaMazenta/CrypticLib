package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

    @Override
    public void load(@NotNull Configuration config) {
        setValue(config.getSection(key));
    }

}
