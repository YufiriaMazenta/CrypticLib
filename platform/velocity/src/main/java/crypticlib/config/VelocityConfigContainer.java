package crypticlib.config;

import crypticlib.config.node.VelocityConfigNode;
import crypticlib.util.ReflectionHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@ApiStatus.Internal
public class VelocityConfigContainer extends ConfigContainer<VelocityConfigWrapper> {

    @ApiStatus.Internal
    public VelocityConfigContainer(@NotNull Class<?> containerClass, @NotNull VelocityConfigWrapper configWrapper) {
        super(containerClass, configWrapper);
    }

    @Override
    public void reload() {
//        configWrapper.reloadConfig(); 不再由ConfigContainer进行重载
        boolean changed = false;
        for (Field field : containerClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()))
                continue;
            Object obj;
            try {
                obj = ReflectionHelper.getDeclaredFieldObj(field, null);
            } catch (IllegalAccessException e) {
                continue;
            }
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
        if (changed) {
            configWrapper.saveConfig();
        }
    }

    @Override
    public @NotNull VelocityConfigWrapper configWrapper() {
        return super.configWrapper();
    }

}
