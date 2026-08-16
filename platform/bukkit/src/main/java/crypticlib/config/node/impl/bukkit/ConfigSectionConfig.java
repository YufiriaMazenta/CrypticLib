package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
    public void load(@NotNull ConfigurationSection config) {
        //load阶段只更新内存value, 不通过setValue把解析结果回写配置对象,
        //键存在但类型不是配置节点时保留文件原值并回退默认值, 避免requireNonNull抛NPE
        if (config.isConfigurationSection(key)) {
            this.value = config.getConfigurationSection(key);
        } else {
            if (config.contains(key)) {
                Bukkit.getLogger().warning("Config value at '" + key + "' in "
                    + configContainer.configWrapper().configFile().getName()
                    + " is not a configuration section, falling back to default value"
                    + " (the original file value is kept).");
            }
            this.value = def;
        }
        setComments(getCommentsFromConfig());
    }

    @Override
    public void saveDef(@NotNull ConfigurationSection config) {
        if (!config.contains(key)) {
            config.set(key, def);
        }
    }
}
