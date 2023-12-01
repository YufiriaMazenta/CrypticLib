package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class ShortListConfigEntry extends ConfigEntry<List<Short>> {

    public ShortListConfigEntry(String key, List<Short> def) {
        super(key, def);
    }

    @Override
    public void load(ConfigurationSection config) {
        saveDef(config);
        setValue(config.getShortList(key()));
    }
}
