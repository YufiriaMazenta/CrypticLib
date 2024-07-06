package crypticlib.config.entry;

import com.electronwill.nightconfig.core.Config;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CharacterListConfig extends ConfigNode<List<Character>> {

    public CharacterListConfig(@NotNull String key, @NotNull List<Character> def) {
        super(key, def);
    }

}
