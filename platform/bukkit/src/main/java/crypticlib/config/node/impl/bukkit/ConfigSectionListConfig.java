package crypticlib.config.node.impl.bukkit;

import crypticlib.util.BukkitConfigHelper;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConfigSectionListConfig extends ListConfig<ConfigurationSection> {

    public ConfigSectionListConfig(@NotNull String key, @NotNull List<ConfigurationSection> def) {
        super(key, def);
    }

    public ConfigSectionListConfig(String key, List<ConfigurationSection> def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public ConfigSectionListConfig(@NotNull String key, @NotNull List<ConfigurationSection> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<ConfigurationSection> readValue(@NotNull ConfigurationSection config) {
        if (!config.isList(key)) {
            return null;
        }
        List<?> list = Objects.requireNonNull(config.getList(key));
        List<ConfigurationSection> value = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof ConfigurationSection) {
                value.add((ConfigurationSection) object);
            } else if (object instanceof Map<?, ?>) {
                value.add(BukkitConfigHelper.map2ConfigSection((Map<?, ?>) object));
            }
        }
        return value;
    }

}
