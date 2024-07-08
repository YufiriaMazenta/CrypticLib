package crypticlib.config;

import crypticlib.config.node.BukkitConfigNode;
import crypticlib.util.ReflectionHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class BukkitConfigContainer extends ConfigContainer<BukkitConfigWrapper> {

    public BukkitConfigContainer(@NotNull Class<?> containerClass, @NotNull BukkitConfigWrapper configWrapper) {
        super(containerClass, configWrapper);
    }

    @Override
    public void reload() {
        configWrapper.reloadConfig();
        for (Field field : containerClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()))
                continue;
            Object obj = ReflectionHelper.getDeclaredFieldObj(field, null);
            if (obj instanceof BukkitConfigNode<?>) {
                BukkitConfigNode<?> config = (BukkitConfigNode<?>) obj;
                if (config.configContainer() == null)
                    config.setConfigContainer(this);
                config.load(configWrapper.config());
            }
        }
        configWrapper.saveConfig();
    }

    @Override
    public @NotNull BukkitConfigWrapper configWrapper() {
        return super.configWrapper();
    }
}
