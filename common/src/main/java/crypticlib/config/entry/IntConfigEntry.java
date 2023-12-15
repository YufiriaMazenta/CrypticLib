package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class IntConfigEntry extends ConfigEntry<Integer> {

    public IntConfigEntry(@NotNull String key, @NotNull Integer def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getInt(key()));
    }
}
