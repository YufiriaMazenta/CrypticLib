package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ShortListConfig extends BungeeConfigNode<List<Short>> {

    public ShortListConfig(@NotNull String key, @NotNull List<Short> def) {
        super(key, def);
    }

    @Override
    protected List<Short> readValue(@NotNull Configuration config) {
        Object raw = config.get(key);
        if (!(raw instanceof List)) {
            return null;
        }
        List<Short> result = new ArrayList<>();
        for (Object element : (List<?>) raw) {
            if (element instanceof Number) {
                result.add(((Number) element).shortValue());
            }
        }
        return result;
    }

}
