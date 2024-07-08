package crypticlib.config.node.impl;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CharacterListConfig extends BukkitConfigNode<List<Character>> {

    public CharacterListConfig(@NotNull String key, @NotNull List<Character> def) {
        super(key, def);
    }

    public CharacterListConfig(@NotNull String key, @NotNull List<Character> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        saveDef(config);
        setValue(config.getCharacterList(key));
        setComments(getCommentsFromConfig());
    }

}
