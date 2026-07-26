package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LongListConfig extends BukkitConfigNode<List<Long>> {

    public LongListConfig(@NotNull String key, @NotNull List<Long> def) {
        super(key, def);
    }

    public LongListConfig(String key, List<Long> def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public LongListConfig(@NotNull String key, @NotNull List<Long> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        //load阶段只更新内存value, 不通过setValue把解析结果回写配置对象,
        //键存在但类型不是列表时保留文件原值并回退默认值, 避免requireNonNull抛NPE
        if (config.isList(key)) {
            this.value = config.getLongList(key);
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
