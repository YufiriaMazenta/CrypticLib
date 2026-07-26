package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ListConfig<T> extends BungeeConfigNode<List<T>> {

    public ListConfig(@NotNull String key, @NotNull List<T> def) {
        super(key, def);
    }

    //load阶段只更新内存value, 不通过setValue把解析结果回写配置对象,
    //键不存在或非列表时回退默认值, 不再requireNonNull抛NPE
    @SuppressWarnings("unchecked")
    @Override
    public void load(@NotNull Configuration config) {
        Object raw = config.get(key);
        if (raw instanceof List) {
            this.value = (List<T>) raw;
        } else {
            if (config.contains(key)) {
                ProxyServer.getInstance().getLogger().warning("Config value at '" + key + "' in "
                    + configContainer.configWrapper().configFile().getName()
                    + " is not a list, falling back to default"
                    + " (the original file value is kept).");
            }
            this.value = def;
        }
    }

}
