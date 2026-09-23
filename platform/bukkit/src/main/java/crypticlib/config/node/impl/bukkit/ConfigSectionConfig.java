package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigSectionConfig extends BukkitConfigNode<ConfigurationSection> {

    public ConfigSectionConfig(@NotNull String key) {
        this(key, new ArrayList<>());
    }

    public ConfigSectionConfig(@NotNull String key, @NotNull String defComment) {
        this(key, new ArrayList<>(Collections.singletonList(defComment)));
    }

    public ConfigSectionConfig(@NotNull String key, @NotNull List<String> defComments) {
        this(key, new MemoryConfiguration(), defComments);
    }

    public ConfigSectionConfig(@NotNull String key, ConfigurationSection def) {
        this(key, def, new ArrayList<>());
    }

    public ConfigSectionConfig(String key, ConfigurationSection def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public ConfigSectionConfig(@NotNull String key, ConfigurationSection def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    protected ConfigurationSection readValue(@NotNull ConfigurationSection config) {
        return config.isConfigurationSection(key) ? config.getConfigurationSection(key) : null;
    }

}
