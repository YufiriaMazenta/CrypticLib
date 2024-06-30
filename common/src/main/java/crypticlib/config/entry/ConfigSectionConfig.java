package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ConfigSectionConfig extends Config<ConfigurationSection> {

    private final Map<String, Object> default_;

    public ConfigSectionConfig(@NotNull String key) {
        this(key, null);
    }

    public ConfigSectionConfig(@NotNull String key, @Nullable Map<String, Object> default_) {
        super(key, null);
        this.default_ = default_;
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        ConfigurationSection configSection;
        if (config.contains(key)) {
            configSection = config.getConfigurationSection(key);
        } else {
            if (default_ != null)
                configSection = config.createSection(key, default_);
            else
                configSection = config.createSection(key);
        }
        setValue(configSection);
    }

}
