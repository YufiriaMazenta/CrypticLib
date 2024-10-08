package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ConfigSectionConfig extends BukkitConfigNode<ConfigurationSection> {

    private final Map<String, Object> default_;

    public ConfigSectionConfig(@NotNull String key) {
        this(key, new ArrayList<>());
    }

    public ConfigSectionConfig(@NotNull String key, @NotNull String defComment) {
        this(key, new ArrayList<>(Collections.singletonList(defComment)));
    }

    public ConfigSectionConfig(@NotNull String key, @NotNull List<String> defComments) {
        this(key, null, defComments);
    }

    public ConfigSectionConfig(@NotNull String key, Map<String, Object> default_) {
        this(key, default_, new ArrayList<>());
    }

    public ConfigSectionConfig(String key, Map<String, Object> default_, @NotNull String defComment) {
        super(key, null, defComment);
        this.default_ = default_;
    }

    public ConfigSectionConfig(@NotNull String key, Map<String, Object> default_, @NotNull List<String> defComments) {
        super(key, null, defComments);
        this.default_ = default_;
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        setValue(Objects.requireNonNull(config.getConfigurationSection(key)));
        setComments(getCommentsFromConfig());
    }

    @Override
    public void saveDef(@NotNull ConfigurationSection config) {
        if (!config.contains(key)) {
            if (default_ != null)
                config.createSection(key, default_);
            else
                config.createSection(key);
        }
    }
}
