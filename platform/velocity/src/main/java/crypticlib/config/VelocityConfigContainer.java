package crypticlib.config;

import crypticlib.config.node.VelocityConfigNode;
import crypticlib.util.ReflectionHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class VelocityConfigContainer extends ConfigContainer<VelocityConfigWrapper> {

    public VelocityConfigContainer(@NotNull Class<?> containerClass, @NotNull VelocityConfigWrapper configWrapper) {
        super(containerClass, configWrapper);
    }

    @Override
    public void reload() {
//        configWrapper.reloadConfig(); 不再由ConfigContainer进行重载
        boolean changed = false;
        for (Class<?> c = containerClass; c != null; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()))
                    continue;
                Object obj = ReflectionHelper.getDeclaredFieldObj(field, null);
                if (obj instanceof VelocityConfigNode<?>) {
                    VelocityConfigNode<?> config = (VelocityConfigNode<?>) obj;
                    if (config.configContainer() == null)
                        config.setConfigContainer(this);
                    if (!configWrapper.config().contains(config.key()))
                        changed = true;
                    config.saveDef(configWrapper.config());
                    config.load(configWrapper.config());
                }
            }
        }
        if (changed) {
            configWrapper.saveConfig();
        }
    }

    @Override
    public @NotNull VelocityConfigWrapper configWrapper() {
        return super.configWrapper();
    }

}
