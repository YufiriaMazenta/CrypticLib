package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ConfigSectionConfig extends BukkitConfigNode<ConfigurationSection> {

    private final Map<String, Object> default_;

    public ConfigSectionConfig(@NotNull String key) {
        this(key, null);
    }

    public ConfigSectionConfig(@NotNull String key, Map<String, Object> default_) {
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
        setComments(config.getComments(key));
    }

}
