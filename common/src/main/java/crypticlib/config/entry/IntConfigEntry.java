package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;

public class IntConfigEntry extends ConfigEntry<Integer> {

    public IntConfigEntry(String key, Integer def) {
        super(key, def);
    }

    @Override
    public void load(ConfigurationSection config) {
        saveDef(config);
        setValue(config.getInt(key()));
    }
}
