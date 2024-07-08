package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CharacterListConfig extends BungeeConfigNode<List<Character>> {

    public CharacterListConfig(@NotNull String key, @NotNull List<Character> def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull Configuration config) {
        saveDef(config);
        setValue(config.getCharList(key));
    }

}
