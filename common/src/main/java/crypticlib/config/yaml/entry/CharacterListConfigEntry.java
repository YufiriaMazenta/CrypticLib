package crypticlib.config.yaml.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CharacterListConfigEntry extends ConfigEntry<List<Character>> {

    public CharacterListConfigEntry(@NotNull String key, @NotNull List<Character> def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getCharacterList(key()));
    }

}
