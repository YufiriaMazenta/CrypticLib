package crypticlib.config.node.impl.bukkit;

import crypticlib.CrypticLib;
import crypticlib.Key;
import crypticlib.util.BukkitConfigHelper;
import org.bukkit.Bukkit;
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

    @Override
    public void load(@NotNull ConfigurationSection config) {
        //load阶段只更新内存value, 不通过setValue把解析结果回写配置对象,
        //键存在但类型不是列表时保留文件原值并回退默认值, 避免requireNonNull抛NPE
        if (config.isList(key)) {
            List<?> list = Objects.requireNonNull(config.getList(key));
            List<ConfigurationSection> value = new ArrayList<>();
            for (Object object : list) {
                if (object instanceof ConfigurationSection) {
                    value.add((ConfigurationSection) object);
                } else if (object instanceof Map<?, ?>) {
                    value.add(BukkitConfigHelper.map2ConfigSection((Map<?, ?>) object));
                } else {
                    Bukkit.getLogger().warning("Failed load config section by " + object);
                }
            }
            this.value = value;
        } else {
            if (config.contains(key)) {
                Bukkit.getLogger().warning("Config value at '" + key + "' in "
                    + configContainer.configWrapper().configFile().getName()
                    + " is not a list, falling back to default value"
                    + " (the original file value is kept).");
            }
            this.value = def;
        }
        setComments(getCommentsFromConfig());
    }

}
