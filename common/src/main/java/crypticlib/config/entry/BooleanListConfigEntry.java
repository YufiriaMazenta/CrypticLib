package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class BooleanListConfigEntry extends ConfigEntry<List<Boolean>> {

    public BooleanListConfigEntry(String key, List<Boolean> def) {
        super(key, def);
    }

    @Override
    public void load(ConfigurationSection config) {
        saveDef(config);
        setValue(config.getBooleanList(key()));
    }

}
