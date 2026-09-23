package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class ByteListConfig extends BukkitConfigNode<List<Byte>> {

    public ByteListConfig(@NotNull String key, @NotNull List<Byte> def) {
        super(key, def);
    }

    public ByteListConfig(String key, List<Byte> def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public ByteListConfig(@NotNull String key, @NotNull List<Byte> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public ByteListConfig(@NotNull String key, @NotNull Supplier<List<Byte>> defFactory) {
        super(key, defFactory);
    }

    public ByteListConfig(@NotNull String key, @NotNull Supplier<List<Byte>> defFactory, @NotNull String defComment) {
        super(key, defFactory, defComment);
    }

    public ByteListConfig(@NotNull String key, @NotNull Supplier<List<Byte>> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    protected List<Byte> readValue(@NotNull ConfigurationSection config) {
        return config.isList(key) ? config.getByteList(key) : null;
    }

}
