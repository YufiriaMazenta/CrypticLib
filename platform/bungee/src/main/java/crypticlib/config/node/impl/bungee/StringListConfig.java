package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class StringListConfig extends BungeeConfigNode<List<String>> {

    public StringListConfig(@NotNull String key, @NotNull List<String> def) {
        super(key, def);
    }

    public StringListConfig(@NotNull String key, @NotNull Supplier<List<String>> defFactory) {
        super(key, defFactory);
    }

    @Override
    protected List<String> readValue(@NotNull Configuration config) {
        Object raw = config.get(key);
        if (!(raw instanceof List)) {
            return null;
        }
        List<String> result = new ArrayList<>();
        for (Object element : (List<?>) raw) {
            if (element instanceof CharSequence) {
                result.add(element.toString());
            }
        }
        return result;
    }

}
