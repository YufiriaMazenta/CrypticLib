package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DoubleListConfig extends BungeeConfigNode<List<Double>> {

    public DoubleListConfig(@NotNull String key, @NotNull List<Double> def) {
        super(key, def);
    }

    @Override
    protected List<Double> readValue(@NotNull Configuration config) {
        Object raw = config.get(key);
        if (!(raw instanceof List)) {
            return null;
        }
        List<Double> result = new ArrayList<>();
        for (Object element : (List<?>) raw) {
            if (element instanceof Number) {
                result.add(((Number) element).doubleValue());
            }
        }
        return result;
    }

}
