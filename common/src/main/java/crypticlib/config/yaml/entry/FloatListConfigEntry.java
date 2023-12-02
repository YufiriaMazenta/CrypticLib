package crypticlib.config.yaml.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FloatListConfigEntry extends ConfigEntry<List<Float>> {

    public FloatListConfigEntry(@NotNull String key, @NotNull List<Float> def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getFloatList(key()));
    }


}
