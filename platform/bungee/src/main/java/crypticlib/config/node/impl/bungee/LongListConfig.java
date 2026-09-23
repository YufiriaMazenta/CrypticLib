package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LongListConfig extends BungeeConfigNode<List<Long>> {

    public LongListConfig(@NotNull String key, @NotNull List<Long> def) {
        super(key, def);
    }

    @Override
    protected List<Long> readValue(@NotNull Configuration config) {
        Object raw = config.get(key);
        if (!(raw instanceof List)) {
            return null;
        }
        List<Long> result = new ArrayList<>();
        for (Object element : (List<?>) raw) {
            if (element instanceof Number) {
                result.add(((Number) element).longValue());
            }
        }
        return result;
    }

}
