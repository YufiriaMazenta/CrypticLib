package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;

public class LongConfigEntry extends ConfigEntry<Long> {

    public LongConfigEntry(String key, Long def) {
        super(key, def);
    }

    @Override
    public void load(ConfigurationSection config) {
        saveDef(config);
        setValue(config.getLong(key()));
    }
}
