package crypticlib.config;

import crypticlib.config.entry.ConfigEntry;
import crypticlib.config.wrapper.YamlConfigWrapper;
import crypticlib.util.ReflectUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class YamlConfigContainer {

    private final Class<?> containerClass;
    private final YamlConfigWrapper configWrapper;

    public YamlConfigContainer(Class<?> containerClass, YamlConfigWrapper configWrapper) {
        this.containerClass = containerClass;
        this.configWrapper = configWrapper;
    }

    public Class<?> containerClass() {
        return containerClass;
    }

    public YamlConfigWrapper configWrapper() {
        return configWrapper;
    }

    public void reload() {
        configWrapper.reloadConfig();
        for (Field field : containerClass.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()))
                continue;
            Object obj = ReflectUtil.getFieldObj(field, null);
            if (obj instanceof ConfigEntry) {
                ((ConfigEntry<?>) obj).load(configWrapper.config());
            }
        }
        configWrapper.saveConfig();
    }

}
