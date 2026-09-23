package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CharacterListConfig extends VelocityConfigNode<List<Character>> {

    public CharacterListConfig(@NotNull String key, @NotNull List<Character> def) {
        super(key, def);
    }

    public CharacterListConfig(@NotNull String key, @NotNull List<Character> def, @NotNull String comment) {
        super(key, def, comment);
    }

    public CharacterListConfig(@NotNull String key, @NotNull List<Character> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    protected List<Character> readValue(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        if (!(raw instanceof List)) {
            return null;
        }
        List<Character> result = new ArrayList<>();
        for (Object element : (List<?>) raw) {
            if (element instanceof Character) {
                result.add((Character) element);
            } else if (element instanceof String && !((String) element).isEmpty()) {
                result.add(((String) element).charAt(0));
            }
        }
        return result;
    }

}
