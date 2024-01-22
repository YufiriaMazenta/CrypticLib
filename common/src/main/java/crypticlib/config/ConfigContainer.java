package crypticlib.config;

import crypticlib.config.entry.ConfigEntry;
import crypticlib.util.ReflectUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ConfigContainer {

    private final Class<?> containerClass;
    private final ConfigWrapper configWrapper;

    public ConfigContainer(@NotNull Class<?> containerClass, @NotNull ConfigWrapper configWrapper) {
        this.containerClass = containerClass;
        this.configWrapper = configWrapper;
    }

    @NotNull
    public Class<?> containerClass() {
        return containerClass;
    }

    @NotNull
    public ConfigWrapper configWrapper() {
        return configWrapper;
    }

    public void reload() {
        configWrapper.reloadConfig();
        for (Field field : containerClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()))
                continue;
            Object obj = ReflectUtil.getDeclaredFieldObj(field, null);
            if (obj instanceof ConfigEntry) {
                ConfigEntry<?> configEntry = (ConfigEntry<?>) obj;
                if (configEntry.configContainer() == null)
                    configEntry.setConfigContainer(this);
                configEntry.load(configWrapper.config());
            }
        }
        configWrapper.saveConfig();
    }

}
