package crypticlib.config.node;

import crypticlib.config.ConfigContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置节点，用于存储一个配置项的内容
 * @param <T> 配置项的数据类型
 * @param <C> 配置项的ConfigurationSection类型
 */
public abstract class ConfigNode<T, C> {

    protected final String key;
    protected final T def;
    protected T value;
    protected List<String> comments;
    protected ConfigContainer configContainer;

    public ConfigNode(@NotNull String key, @NotNull T def) {
        this.key = key;
        this.def = def;
        this.comments = new ArrayList<>();
    }

    @NotNull
    public T value() {
        return value;
    }

    public void setValue(@NotNull T value) {
        this.value = value;
    }

    @NotNull
    public T def() {
        return def;
    }

    @NotNull
    public String key() {
        return key;
    }

    public ConfigContainer configContainer() {
        return configContainer;
    }

    public ConfigNode<T, C> setConfigContainer(ConfigContainer configContainer) {
        this.configContainer = configContainer;
        return this;
    }

    public void saveConfig() {
        configContainer.configWrapper().setComments(key, comments);
        configContainer.configWrapper().set(key, value);
        configContainer.configWrapper().saveConfig();
    }

    public void setComments(@Nullable List<String> comments) {
        this.comments = comments;
    }

    public @Nullable List<String> getComments() {
        return comments;
    }

    public abstract void load(@NotNull C config);

    public abstract void saveDef(@NotNull C config);

}
