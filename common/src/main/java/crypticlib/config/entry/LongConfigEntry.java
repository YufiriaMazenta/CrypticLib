package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class LongConfigEntry extends ConfigEntry<Long> {

    public LongConfigEntry(@NotNull String key, @NotNull Long def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getLong(key()));
    }
}
