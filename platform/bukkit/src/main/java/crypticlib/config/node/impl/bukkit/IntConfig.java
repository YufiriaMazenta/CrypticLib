package crypticlib.config.node.impl.bukkit;

import crypticlib.config.node.BukkitConfigNode;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IntConfig extends BukkitConfigNode<Integer> {

    public IntConfig(@NotNull String key, @NotNull Integer def) {
        super(key, def);
    }

    public IntConfig(String key, Integer def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public IntConfig(@NotNull String key, @NotNull Integer def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        //load阶段只更新内存value, 不通过setValue把解析结果回写配置对象,
        //避免类型不匹配时(如把整数误写成字符串)用0静默覆盖用户原值
        if (config.isInt(key)) {
            this.value = config.getInt(key);
        } else {
            if (config.contains(key)) {
                Bukkit.getLogger().warning("Config value at '" + key + "' in "
                    + configContainer.configWrapper().configFile().getName()
                    + " is not an integer, falling back to default " + def
                    + " (the original file value is kept).");
            }
            this.value = def;
        }
        setComments(getCommentsFromConfig());
    }

}
