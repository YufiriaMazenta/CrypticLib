package crypticlib.config.entry;

import crypticlib.config.ConfigContainer;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public abstract class Config<T> {

    protected final String key;
    protected final T def;
    protected T value;
    protected ConfigContainer configContainer;

    public Config(@NotNull String key, @NotNull T def) {
        this.key = key;
        this.def = def;
    }

    @NotNull
    public T value() {
        return value;
    }

    public Config<T> setValue(@NotNull T value) {
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

    public Config<T> setConfigContainer(ConfigContainer configContainer) {
        this.configContainer = configContainer;
        return this;
    }

    public void saveDef(@NotNull ConfigurationSection config) {
        if (config.contains(key))
            return;
        config.set(key, def);
    }

    public void saveConfig() {
        configContainer.configWrapper().set(key, value);
        configContainer.configWrapper().saveConfig();
    }

    public abstract void load(@NotNull ConfigurationSection config);

}
