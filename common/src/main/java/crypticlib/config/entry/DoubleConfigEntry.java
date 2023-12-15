package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class DoubleConfigEntry extends ConfigEntry<Double> {
    public DoubleConfigEntry(@NotNull String key, @NotNull Double def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getDouble(key()));
    }
}
