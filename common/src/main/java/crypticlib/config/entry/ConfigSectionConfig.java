package crypticlib.config.entry;

import com.electronwill.nightconfig.core.Config;
import org.jetbrains.annotations.NotNull;

public class ConfigSectionConfig extends ConfigNode<Config> {

    private final Config default_;

    public ConfigSectionConfig(@NotNull String key) {
        this(key, null);
    }

    public ConfigSectionConfig(@NotNull String key, Config default_) {
        super(key, default_);
        this.default_ = default_;
    }

    @Override
    public void load(@NotNull Config config) {
        Config configSection;
        if (config.contains(key)) {
            configSection = config.get(key);
        } else {
            if (default_ != null) {
                config.set(key, default_);
                configSection = config.get(key);
            }
            else {
                configSection = config.createSubConfig();
            }
        }
        setValue(configSection);
    }

}
