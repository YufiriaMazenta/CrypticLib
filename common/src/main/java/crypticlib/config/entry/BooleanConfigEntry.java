package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;

public class BooleanConfigEntry extends ConfigEntry<Boolean> {

    public BooleanConfigEntry(String key, Boolean def) {
        super(key, def);
    }

    @Override
    public void load(ConfigurationSection config) {
        saveDef(config);
        setValue(config.getBoolean(key()));
    }

}
