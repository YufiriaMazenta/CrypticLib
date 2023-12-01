package crypticlib.config.entry;

import org.bukkit.configuration.ConfigurationSection;

public abstract class ConfigEntry<T> {

    private final String key;
    private T value;
    private final T def;

    public ConfigEntry(String key, T def) {
        this.key = key;
        this.def = def;
    }

    public T value() {
        return value;
    }

    public ConfigEntry<T> setValue(T value) {
        this.value = value;
        return this;
    }

    protected T def() {
        return def;
    }

    protected String key() {
        return key;
    }

    public void saveDef(ConfigurationSection config) {
        if (config.contains(key))
            return;
        config.set(key, def);
    }

    public abstract void load(ConfigurationSection config);

}
