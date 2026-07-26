package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ListConfig<T> extends BukkitConfigNode<List<T>> {

    public ListConfig(@NotNull String key, @NotNull List<T> def) {
        super(key, def);
    }

    public ListConfig(String key, List<T> def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public ListConfig(@NotNull String key, @NotNull List<T> def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void load(@NotNull ConfigurationSection config) {
        //load阶段只更新内存value, 不通过setValue把解析结果回写配置对象,
        //键存在但类型不是列表时保留文件原值并回退默认值, 避免requireNonNull抛NPE
        if (config.isList(key)) {
            this.value = (List<T>) config.getList(key);
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
