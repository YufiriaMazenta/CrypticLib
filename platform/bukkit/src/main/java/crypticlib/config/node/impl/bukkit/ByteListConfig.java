package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

    @Override
    public void load(@NotNull ConfigurationSection config) {
        setValue(config.getByteList(key));
        setComments(getCommentsFromConfig());
    }

}
