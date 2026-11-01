package crypticlib.config.node;

import crypticlib.MinecraftVersion;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public abstract class BukkitConfigNode<T> extends ConfigNode<T, ConfigurationSection> {

    public BukkitConfigNode(@NotNull String key, @NotNull T def) {
        super(key, def);
    }

    public BukkitConfigNode(String key, T def, @NotNull String defComment) {
        super(key, def, defComment);
    }

    public BukkitConfigNode(@NotNull String key, @NotNull T def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    public BukkitConfigNode(@NotNull String key, @NotNull Supplier<T> defFactory) {
        super(key, defFactory);
    }

    public BukkitConfigNode(@NotNull String key, @NotNull Supplier<T> defFactory, @NotNull String defComment) {
        super(key, defFactory, defComment);
    }

    public BukkitConfigNode(@NotNull String key, @NotNull Supplier<T> defFactory, @NotNull List<String> defComments) {
        super(key, defFactory, defComments);
    }

    @Override
    public void saveDef(@NotNull ConfigurationSection config) {
        //加载默认值
        if (!config.contains(key)) {
            setValue(def);
        }

        //加载默认注释
        if (MinecraftVersion.current().afterOrEquals(MinecraftVersion.V1_18_1)) {
            if (defComments == null) {
                return;
            }
            if (!config.getComments(key).isEmpty()) {
                return;
            }
            setComments(defComments);
        }
    }

    /**
     * 判断 {@link #saveDef(ConfigurationSection)} 是否会写入内容(缺键或缺少默认注释)。
     * <p>
     * 调用方据此决定是否需要落盘: 若只跟踪"缺键", 那么给一个已存在的键新增默认注释时
     * 只会写进内存而永远不会保存, 文档里的默认注释对老用户永远不出现。
     *
     * @param config 配置
     * @return 是否会产生写入
     */
    public boolean wouldWriteDef(@NotNull ConfigurationSection config) {
        if (!config.contains(key)) {
            return true;
        }
        if (!MinecraftVersion.current().afterOrEquals(MinecraftVersion.V1_18_1)) {
            return false;
        }
        return defComments != null && !defComments.isEmpty() && config.getComments(key).isEmpty();
    }

    @Override
    protected final boolean hasKey(@NotNull ConfigurationSection config) {
        return config.contains(key);
    }

    @Override
    protected List<String> readComments() {
        return configContainer.configWrapper().getComments(key);
    }

}
