package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigSectionConfigEntry extends ConfigEntry<ConfigurationSection> {

    public ConfigSectionConfigEntry(@NotNull String key, @Nullable ConfigurationSection def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        ConfigurationSection configSection;
        if (config.contains(key))
            configSection = config.getConfigurationSection(key);
        else
            configSection = config.createSection(key);
        saveDef(config);
        setValue(configSection);
    }
}
