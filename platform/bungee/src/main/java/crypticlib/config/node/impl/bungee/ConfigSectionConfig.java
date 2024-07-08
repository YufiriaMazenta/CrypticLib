package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

public class ConfigSectionConfig extends BungeeConfigNode<Configuration> {

    public ConfigSectionConfig(@NotNull String key) {
        this(key, new Configuration());
    }

    public ConfigSectionConfig(@NotNull String key, @NotNull Configuration def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull Configuration config) {
        saveDef(config);
        setValue(config.getSection(key));
    }

}
