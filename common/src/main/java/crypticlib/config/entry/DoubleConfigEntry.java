package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;

public class DoubleConfigEntry extends ConfigEntry<Double> {
    public DoubleConfigEntry(String key, Double def) {
        super(key, def);
    }

    @Override
    public void load(ConfigurationSection config) {
        saveDef(config);
        setValue(config.getDouble(key()));
    }
}
