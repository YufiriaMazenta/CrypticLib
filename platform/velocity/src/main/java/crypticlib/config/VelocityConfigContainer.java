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
        configWrapper.reloadConfig();
        for (Field field : containerClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()))
                continue;
            Object obj = ReflectionHelper.getDeclaredFieldObj(field, null);
            if (obj instanceof VelocityConfigNode<?>) {
                VelocityConfigNode<?> config = (VelocityConfigNode<?>) obj;
                if (config.configContainer() == null)
                    config.setConfigContainer(this);
                config.saveDef(configWrapper.config());
                config.load(configWrapper.config());
            }
        }
        configWrapper.saveConfig();
    }

    @Override
    public @NotNull VelocityConfigWrapper configWrapper() {
        return super.configWrapper();
    }

}
