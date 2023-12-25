package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BooleanListConfigEntry extends ConfigEntry<List<Boolean>> {

    public BooleanListConfigEntry(@NotNull String key, @NotNull List<Boolean> def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getBooleanList(key));
    }

}
