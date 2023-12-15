package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StringListConfigEntry extends ConfigEntry<List<String>> {

    public StringListConfigEntry(@NotNull String key, @NotNull List<String> def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getStringList(key()));
    }
}
