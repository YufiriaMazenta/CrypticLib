package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public abstract class ConfigEntry<T> {

    private final String key;
    private final T def;
    private T value;

    public ConfigEntry(@NotNull String key, @NotNull T def) {
        this.key = key;
        this.def = def;
    }

    @NotNull
    public T value() {
        return value;
    }

    public ConfigEntry<T> setValue(@NotNull T value) {
        this.value = value;
        return this;
    }

    @NotNull
    protected T def() {
        return def;
    }

    @NotNull
    protected String key() {
        return key;
    }

    public void saveDef(@NotNull ConfigurationSection config) {
        if (config.contains(key))
            return;
        config.set(key, def);
    }

    public abstract void load(@NotNull ConfigurationSection config);

}
