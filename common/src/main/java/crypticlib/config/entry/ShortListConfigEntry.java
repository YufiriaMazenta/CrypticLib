package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShortListConfigEntry extends ConfigEntry<List<Short>> {

    public ShortListConfigEntry(@NotNull String key, @NotNull List<Short> def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getShortList(key()));
    }
}
