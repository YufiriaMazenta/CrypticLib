package crypticlib.config.node;

import crypticlib.config.ConfigContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 配置节点，用于存储一个配置项的内容
 * @param <T> 配置项的数据类型
 * @param <C> 配置项的ConfigurationSection类型
 */
public abstract class ConfigNode<T, C> {

    protected final String key;
    protected final T def;
    protected final List<String> defComments;
    protected T value;
    protected List<String> comments;
    protected ConfigContainer<?> configContainer;

    public ConfigNode(String key, T def) {
        this(key, def, new ArrayList<>());
    }

    public ConfigNode(String key, T def, @NotNull String defComment) {
        this(key, def, new ArrayList<>(Collections.singletonList(defComment)));
    }


    public ConfigNode(@NotNull String key, @NotNull T def, @NotNull List<String> defComments) {
        this.key = key;
        this.def = def;
        this.defComments = defComments;
    }

    @NotNull
    public T value() {
        return value;
    }

    public void setValue(@NotNull T value) {
        this.value = value;
        configContainer.configWrapper().set(key, value);
    }

    @NotNull
    public T def() {
        return def;
    }

    public List<String> getDefComments() {
        return defComments;
    }

    @NotNull
    public String key() {
        return key;
    }

    public ConfigContainer<?> configContainer() {
        return configContainer;
    }

    public ConfigNode<T, C> setConfigContainer(ConfigContainer<?> configContainer) {
        this.configContainer = configContainer;
        return this;
    }

    public void saveConfig() {
        configContainer.configWrapper().saveConfig();
    }

    public void setComments(@Nullable List<String> comments) {
        this.comments = comments;
        configContainer.configWrapper().setComments(key, comments);
    }

    public void setComment(@Nullable String comment) {
        setComments(Collections.singletonList(comment));
    }

    public @Nullable List<String> getComments() {
        return comments;
    }

    public abstract void load(@NotNull C config);

    public abstract void saveDef(@NotNull C config);

}
