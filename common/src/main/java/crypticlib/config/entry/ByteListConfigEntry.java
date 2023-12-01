package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class ByteListConfigEntry extends ConfigEntry<List<Byte>> {

    public ByteListConfigEntry(String key, List<Byte> def) {
        super(key, def);
    }

    @Override
    public void load(ConfigurationSection config) {
        saveDef(config);
        setValue(config.getByteList(key()));
    }
}
