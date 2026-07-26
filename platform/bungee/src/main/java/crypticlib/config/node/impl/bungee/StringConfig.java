package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

public class StringConfig extends BungeeConfigNode<String> {

    public StringConfig(@NotNull String key, @NotNull String def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull Configuration config) {
        //load阶段只更新内存value, 不通过setValue把解析结果回写配置对象,
        //避免类型不匹配时用默认值静默覆盖用户原值
        Object raw = config.get(key);
        if (raw instanceof CharSequence) {
            this.value = raw.toString();
        } else {
            if (config.contains(key)) {
                ProxyServer.getInstance().getLogger().warning("Config value at '" + key + "' in "
                    + configContainer.configWrapper().configFile().getName()
                    + " is not a string, falling back to default " + def
                    + " (the original file value is kept).");
            }
            this.value = def;
        }
    }

}
