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
    public void load(@NotNull CommentedConfig config) {
        Object raw = config.get(key);
        if (raw instanceof List) {
            List<Character> value = new ArrayList<>();
            for (Object element : (List<?>) raw) {
                if (element instanceof Character) {
                    value.add((Character) element);
                } else if (element instanceof String && !((String) element).isEmpty()) {
                    value.add(((String) element).charAt(0));
                }
            }
            setValue(value);
        } else {
            setValue(def);
        }
        setComments(configContainer.configWrapper().getComments(key));
    }

}
