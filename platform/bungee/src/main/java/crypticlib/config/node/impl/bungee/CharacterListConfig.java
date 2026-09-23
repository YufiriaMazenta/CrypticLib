package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CharacterListConfig extends BungeeConfigNode<List<Character>> {

    public CharacterListConfig(@NotNull String key, @NotNull List<Character> def) {
        super(key, def);
    }

    @Override
    protected List<Character> readValue(@NotNull Configuration config) {
        Object raw = config.get(key);
        if (!(raw instanceof List)) {
            return null;
        }
        List<Character> result = new ArrayList<>();
        for (Object element : (List<?>) raw) {
            if (element instanceof Character) {
                result.add((Character) element);
            } else if (element instanceof CharSequence && ((CharSequence) element).length() > 0) {
                result.add(((CharSequence) element).charAt(0));
            }
        }
        return result;
    }

}
