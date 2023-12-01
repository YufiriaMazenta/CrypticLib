package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class IntListConfigEntry extends ConfigEntry<List<Integer>> {

    public IntListConfigEntry(String key, List<Integer> def) {
        super(key, def);
    }

    @Override
    public void load(ConfigurationSection config) {
        saveDef(config);
        setValue(config.getIntegerList(key()));
    }

}
