package crypticlib.config.node.impl.bungee;

import crypticlib.config.node.BungeeConfigNode;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ByteListConfig extends BungeeConfigNode<List<Byte>> {

    public ByteListConfig(@NotNull String key, @NotNull List<Byte> def) {
        super(key, def);
    }

    //load阶段只更新内存value, 不通过setValue把解析结果回写配置对象,
    //逐元素类型转换避免CCE, 键不存在或非列表时回退默认值
    @Override
    public void load(@NotNull Configuration config) {
        Object raw = config.get(key);
        if (raw instanceof List) {
            List<Byte> result = new ArrayList<>();
            for (Object element : (List<?>) raw) {
                if (element instanceof Number) {
                    result.add(((Number) element).byteValue());
                }
            }
            this.value = result;
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
