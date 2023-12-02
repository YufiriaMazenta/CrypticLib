package crypticlib.config.yaml.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class StringConfigEntry extends ConfigEntry<String> {

    public StringConfigEntry(@NotNull String key, @NotNull String def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getString(key(), def()));
    }

}
