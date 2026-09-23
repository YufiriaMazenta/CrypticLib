package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class CharacterListConfig extends BukkitConfigNode<List<Character>> {

    public CharacterListConfig(@NotNull String key, @NotNull List<Character> def) {
        super(key, def);
    }

    public CharacterListConfig(String key, List<Character> def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public CharacterListConfig(@NotNull String key, @NotNull List<Character> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public CharacterListConfig(@NotNull String key, @NotNull Supplier<List<Character>> defFactory) {
        super(key, defFactory);
    }

    public CharacterListConfig(@NotNull String key, @NotNull Supplier<List<Character>> defFactory, @NotNull String defComment) {
        super(key, defFactory, defComment);
    }

    public CharacterListConfig(@NotNull String key, @NotNull Supplier<List<Character>> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    protected List<Character> readValue(@NotNull ConfigurationSection config) {
        return config.isList(key) ? config.getCharacterList(key) : null;
    }

}
