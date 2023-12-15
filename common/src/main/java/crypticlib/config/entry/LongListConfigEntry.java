package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LongListConfigEntry extends ConfigEntry<List<Long>> {

    public LongListConfigEntry(@NotNull String key, @NotNull List<Long> def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getLongList(key()));
    }
}
