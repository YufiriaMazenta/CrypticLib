package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.Config;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

public class ConfigSectionConfig extends VelocityConfigNode<Config> {

    public ConfigSectionConfig(@NotNull String key) {
        this(key, Config.inMemory());
    }

    public ConfigSectionConfig(@NotNull String key, @NotNull Config def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull Config config) {
        setValue(config.getOrElse(key, def));
    }

}
