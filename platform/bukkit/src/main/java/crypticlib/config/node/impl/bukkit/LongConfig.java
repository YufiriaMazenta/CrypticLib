package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LongConfig extends BukkitConfigNode<Long> {

    public LongConfig(@NotNull String key, @NotNull Long def) {
        super(key, def);
    }

    public LongConfig(String key, Long def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public LongConfig(@NotNull String key, @NotNull Long def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        //load阶段只更新内存value, 不通过setValue把解析结果回写配置对象,
        //避免类型不匹配时用0静默覆盖用户原值
        Object raw = config.get(key);
        if (raw instanceof Number) {
            this.value = ((Number) raw).longValue();
        } else {
            if (config.contains(key)) {
                Bukkit.getLogger().warning("Config value at '" + key + "' in "
                    + configContainer.configWrapper().configFile().getName()
                    + " is not a number, falling back to default " + def
                    + " (the original file value is kept).");
            }
            this.value = def;
        }
        setComments(getCommentsFromConfig());
    }

}
