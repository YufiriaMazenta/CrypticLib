package crypticlib.config;

import crypticlib.config.node.ConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ConfigContainer<C extends ConfigWrapper<?>, Node extends ConfigNode<?, ?>> {

    protected final Class<?> containerClass;
    protected final C configWrapper;
    protected final Map<String, Node> configNodeMap;

    public ConfigContainer(@NotNull Class<?> containerClass, @NotNull C configWrapper) {
        this.containerClass = containerClass;
        this.configWrapper = configWrapper;
        this.configNodeMap = new ConcurrentHashMap<>();
        scanConfigNodes();
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

    public abstract void scanConfigNodes();

}
