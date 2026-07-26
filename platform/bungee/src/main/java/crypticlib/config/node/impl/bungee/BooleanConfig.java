package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

public class BooleanConfig extends BungeeConfigNode<Boolean> {

    public BooleanConfig(@NotNull String key, @NotNull Boolean def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull Configuration config) {
        //load阶段只更新内存value, 不通过setValue把解析结果回写配置对象,
        //避免类型不匹配时用false静默覆盖用户原值
        Object raw = config.get(key);
        if (raw instanceof Boolean) {
            this.value = (Boolean) raw;
        } else {
            if (config.contains(key)) {
                ProxyServer.getInstance().getLogger().warning("Config value at '" + key + "' in "
                    + configContainer.configWrapper().configFile().getName()
                    + " is not a boolean, falling back to default " + def
                    + " (the original file value is kept).");
            }
            this.value = def;
        }
    }

}
