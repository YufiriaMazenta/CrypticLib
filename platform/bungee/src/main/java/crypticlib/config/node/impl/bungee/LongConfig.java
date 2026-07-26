package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

public class LongConfig extends BungeeConfigNode<Long> {

    public LongConfig(@NotNull String key, @NotNull Long def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull Configuration config) {
        //load阶段只更新内存value, 不通过setValue把解析结果回写配置对象,
        //避免类型不匹配时(如把整数误写成字符串)用0静默覆盖用户原值
        Object raw = config.get(key);
        if (raw instanceof Number) {
            this.value = ((Number) raw).longValue();
        } else {
            if (config.contains(key)) {
                ProxyServer.getInstance().getLogger().warning("Config value at '" + key + "' in "
                    + configContainer.configWrapper().configFile().getName()
                    + " is not a long, falling back to default " + def
                    + " (the original file value is kept).");
            }
            this.value = def;
        }
    }

}
