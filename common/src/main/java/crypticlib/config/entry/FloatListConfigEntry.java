package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class FloatListConfigEntry extends ConfigEntry<List<Float>> {

    public FloatListConfigEntry(String key, List<Float> def) {
        super(key, def);
    }

    @Override
    public void load(ConfigurationSection config) {
        saveDef(config);
        setValue(config.getFloatList(key()));
    }


}
