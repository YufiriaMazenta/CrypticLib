package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class CharacterListConfigEntry extends ConfigEntry<List<Character>> {

    public CharacterListConfigEntry(String key, List<Character> def) {
        super(key, def);
    }

    @Override
    public void load(ConfigurationSection config) {
        saveDef(config);
        setValue(config.getCharacterList(key()));
    }

}
