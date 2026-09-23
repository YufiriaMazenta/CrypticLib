package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FloatListConfig extends BungeeConfigNode<List<Float>> {

    public FloatListConfig(@NotNull String key, @NotNull List<Float> def) {
        super(key, def);
    }

    @Override
    protected List<Float> readValue(@NotNull Configuration config) {
        Object raw = config.get(key);
        if (!(raw instanceof List)) {
            return null;
        }
        List<Float> result = new ArrayList<>();
        for (Object element : (List<?>) raw) {
            if (element instanceof Number) {
                result.add(((Number) element).floatValue());
            }
        }
        return result;
    }

}
