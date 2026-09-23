package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BooleanListConfig extends BungeeConfigNode<List<Boolean>> {

    public BooleanListConfig(@NotNull String key, @NotNull List<Boolean> def) {
        super(key, def);
    }

    public BooleanListConfig(@NotNull String key, @NotNull Supplier<List<Boolean>> defFactory) {
        super(key, defFactory);
    }

    @Override
    protected List<Boolean> readValue(@NotNull Configuration config) {
        Object raw = config.get(key);
        if (!(raw instanceof List)) {
            return null;
        }
        List<Boolean> result = new ArrayList<>();
        for (Object element : (List<?>) raw) {
            if (element instanceof Boolean) {
                result.add((Boolean) element);
            }
        }
        return result;
    }

}
