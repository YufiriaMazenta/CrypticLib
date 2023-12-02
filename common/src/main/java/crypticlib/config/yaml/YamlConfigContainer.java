package crypticlib.config.yaml;

import crypticlib.config.yaml.entry.ConfigEntry;
import crypticlib.util.ReflectUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class YamlConfigContainer {

    private final Class<?> containerClass;
    private final YamlConfigWrapper configWrapper;

    public YamlConfigContainer(@NotNull Class<?> containerClass, @NotNull YamlConfigWrapper configWrapper) {
        this.containerClass = containerClass;
        this.configWrapper = configWrapper;
    }

    @NotNull
    public Class<?> containerClass() {
        return containerClass;
    }

    @NotNull
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
