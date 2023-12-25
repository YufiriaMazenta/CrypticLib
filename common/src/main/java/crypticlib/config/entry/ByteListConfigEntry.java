package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ByteListConfigEntry extends ConfigEntry<List<Byte>> {

    public ByteListConfigEntry(@NotNull String key, @NotNull List<Byte> def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getByteList(key));
    }
}
