package crypticlib.config;

import crypticlib.config.node.BungeeConfigNode;
import crypticlib.util.ReflectionHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class BungeeConfigContainer extends ConfigContainer<BungeeConfigWrapper> {

    public BungeeConfigContainer(@NotNull Class<?> containerClass, @NotNull BungeeConfigWrapper configWrapper) {
        super(containerClass, configWrapper);
    }

    @Override
    public void reload() {
        configWrapper.reloadConfig();
        for (Field field : containerClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()))
                continue;
            Object obj = ReflectionHelper.getDeclaredFieldObj(field, null);
            if (obj instanceof BungeeConfigNode<?>) {
                BungeeConfigNode<?> config = (BungeeConfigNode<?>) obj;
                if (config.configContainer() == null)
                    config.setConfigContainer(this);
                config.load(configWrapper.config());
            }
        }
        configWrapper.saveConfig();
    }

    @Override
    public @NotNull BungeeConfigWrapper configWrapper() {
        return super.configWrapper();
    }
}
