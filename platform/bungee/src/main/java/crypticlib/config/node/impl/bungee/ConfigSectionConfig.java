package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ConfigSectionConfig extends BungeeConfigNode<Configuration> {

    public ConfigSectionConfig(@NotNull String key) {
        this(key, new HashMap<>());
    }

    public ConfigSectionConfig(@NotNull String key, @NotNull Map<String, Object> def) {
        super(key, new Configuration());
        for (Map.Entry<String, Object> entry : def.entrySet()) {
            this.def.set(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void load(@NotNull Configuration config) {
        //load阶段只更新内存value, 不通过setValue把解析结果回写配置对象,
        //避免类型不匹配时用空节点静默覆盖用户原值
        Object raw = config.get(key);
        if (raw instanceof Configuration) {
            this.value = (Configuration) raw;
        } else {
            if (config.contains(key)) {
                ProxyServer.getInstance().getLogger().warning("Config value at '" + key + "' in "
                    + configContainer.configWrapper().configFile().getName()
                    + " is not a config section, falling back to default"
                    + " (the original file value is kept).");
            }
            this.value = def;
        }
    }

}
