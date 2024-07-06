package crypticlib.config.entry;

import com.electronwill.nightconfig.core.Config;
import crypticlib.config.ConfigContainer;
import org.jetbrains.annotations.NotNull;

public abstract class ConfigNode<T> {

    protected final String key;
    protected final T def;
    protected T value;
    protected ConfigContainer configContainer;

    public ConfigNode(@NotNull String key, @NotNull T def) {
        this.key = key;
        this.def = def;
    }

    @NotNull
    public T value() {
        return value;
    }

    public ConfigNode<T> setValue(@NotNull T value) {
        this.value = value;
        return this;
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

    public ConfigNode<T> setConfigContainer(ConfigContainer configContainer) {
        this.configContainer = configContainer;
        return this;
    }

    public void saveDef(@NotNull Config config) {
        if (config.contains(key))
            return;
        config.set(key, def);
    }

    public void saveConfig() {
        configContainer.configWrapper().set(key, value);
        configContainer.configWrapper().saveConfig();
    }

    public void load(@NotNull Config config) {
        saveDef(config);
        setValue(config.get(key));
    }

}
