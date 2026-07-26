package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BooleanConfig extends BukkitConfigNode<Boolean> {

    public BooleanConfig(@NotNull String key, @NotNull Boolean def) {
        super(key, def);
    }

    public BooleanConfig(String key, Boolean def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public BooleanConfig(@NotNull String key, @NotNull Boolean def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        //load阶段只更新内存value, 不通过setValue把解析结果回写配置对象,
        //避免类型不匹配时(如把布尔误写成字符串)用false静默覆盖用户原值
        if (config.isBoolean(key)) {
            this.value = config.getBoolean(key);
        } else {
            if (config.contains(key)) {
                Bukkit.getLogger().warning("Config value at '" + key + "' in "
                    + configContainer.configWrapper().configFile().getName()
                    + " is not a boolean, falling back to default " + def
                    + " (the original file value is kept).");
            }
            this.value = def;
        }
        setComments(getCommentsFromConfig());
    }

}
