package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class DoubleConfig extends Config<Double> {
    public DoubleConfig(@NotNull String key, @NotNull Double def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getDouble(key));
    }
}
