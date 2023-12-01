package crypticlib.config.yaml.entry;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class LongListConfigEntry extends ConfigEntry<List<Long>> {

    public LongListConfigEntry(String key, List<Long> def) {
        super(key, def);
    }

    @Override
    public void load(ConfigurationSection config) {
        saveDef(config);
        setValue(config.getLongList(key()));
    }
}
