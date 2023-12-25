package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IntListConfigEntry extends ConfigEntry<List<Integer>> {

    public IntListConfigEntry(@NotNull String key, @NotNull List<Integer> def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getIntegerList(key));
    }

}
