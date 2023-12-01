package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;

public class StringConfigEntry extends ConfigEntry<String> {

    public StringConfigEntry(String key, String def) {
        super(key, def);
    }

    @Override
    public void load(ConfigurationSection config) {
        saveDef(config);
        setValue(config.getString(key()));
    }

}
