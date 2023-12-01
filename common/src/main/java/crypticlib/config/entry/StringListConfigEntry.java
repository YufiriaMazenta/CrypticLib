package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class StringListConfigEntry extends ConfigEntry<List<String>> {

    public StringListConfigEntry(String key, List<String> def) {
        super(key, def);
    }

    @Override
    public void load(ConfigurationSection config) {
        saveDef(config);
    }
}
