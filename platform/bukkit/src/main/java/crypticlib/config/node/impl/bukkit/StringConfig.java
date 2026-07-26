package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StringConfig extends BukkitConfigNode<String> {

    public StringConfig(@NotNull String key, @NotNull String def) {
        super(key, def);
    }

    public StringConfig(String key, String def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public StringConfig(@NotNull String key, @NotNull String def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        //load阶段只更新内存value, 不通过setValue把解析结果回写配置对象
        if (config.isString(key)) {
            this.value = config.getString(key, def);
        } else {
            if (config.contains(key)) {
                Bukkit.getLogger().warning("Config value at '" + key + "' in "
                    + configContainer.configWrapper().configFile().getName()
                    + " is not a string, falling back to default " + def
                    + " (the original file value is kept).");
            }
            this.value = def;
        }
        setComments(getCommentsFromConfig());
    }

}
