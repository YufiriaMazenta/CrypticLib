package crypticlib.config;

import crypticlib.config.node.VelocityConfigNode;
import crypticlib.util.ReflectionHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApiStatus.Internal
public class VelocityConfigContainer extends ConfigContainer<VelocityConfigWrapper, VelocityConfigNode<?>> {

    @ApiStatus.Internal
    public VelocityConfigContainer(@NotNull Class<?> containerClass, @NotNull VelocityConfigWrapper configWrapper) {
        super(containerClass, configWrapper);
    }

    @Override
    public void reload() {
//        configWrapper.reloadConfig(); 不再由ConfigContainer进行重载
        boolean changed = false;
        for (VelocityConfigNode<?> configNode : configNodeMap.values()) {
            if (configNode.configContainer() == null)
                configNode.setConfigContainer(this);
            if (!configWrapper.config().contains(configNode.key()))
                changed = true;
            configNode.saveDef(configWrapper.config());
            configNode.load(configWrapper.config());
        }
        if (changed) {
            configWrapper.saveConfig();
        }
    }

    @Override
    public void scanConfigNodes() {
        configNodeMap.clear();
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
                configNodeMap.put(config.key(), config);
            }
        }
    }

    @Override
    public @NotNull VelocityConfigWrapper configWrapper() {
        return super.configWrapper();
    }

}
