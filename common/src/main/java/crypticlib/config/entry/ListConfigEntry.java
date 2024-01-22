package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ListConfigEntry extends ConfigEntry<List<?>> {

    public ListConfigEntry(@NotNull String key, @NotNull List<?> def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getList(key, def));
    }

}
