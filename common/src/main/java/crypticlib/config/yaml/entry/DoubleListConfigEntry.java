package crypticlib.config.yaml.entry;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class DoubleListConfigEntry extends ConfigEntry<List<Double>> {

    public DoubleListConfigEntry(String key, List<Double> def) {
        super(key, def);
    }

    @Override
    public void load(ConfigurationSection config) {
        saveDef(config);
        setValue(config.getDoubleList(key()));
    }

}
