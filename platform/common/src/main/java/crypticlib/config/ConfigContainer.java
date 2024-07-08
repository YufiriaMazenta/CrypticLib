package crypticlib.config;

import org.jetbrains.annotations.NotNull;

public abstract class ConfigContainer<C extends ConfigWrapper<?>> {

    protected final Class<?> containerClass;
    protected final C configWrapper;

    public ConfigContainer(@NotNull Class<?> containerClass, @NotNull C configWrapper) {
        this.containerClass = containerClass;
        this.configWrapper = configWrapper;
    }

    @NotNull
    public Class<?> containerClass() {
        return containerClass;
    }

    @NotNull
    public C configWrapper() {
        return configWrapper;
    }

    public abstract void reload();

}
