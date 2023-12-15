package crypticlib.chat;

import crypticlib.config.ConfigWrapper;
import crypticlib.util.ReflectUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

public class LangConfigContainer {

    private final Class<?> containerClass;
    private final Map<String, ConfigWrapper> langConfigWrapperMap;

    public LangConfigContainer(@NotNull Class<?> containerClass, Map<String, ConfigWrapper> langConfigWrapperMap) {
        this.langConfigWrapperMap = langConfigWrapperMap;
        this.containerClass = containerClass;
    }

    public Class<?> containerClass() {
        return containerClass;
    }

    public Map<String, ConfigWrapper> langConfigWrapperMap() {
        return langConfigWrapperMap;
    }

    public void reload() {
        langConfigWrapperMap.forEach((lang, configWrapper) -> {
            configWrapper.reloadConfig();
        });
        for (Field field : containerClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()))
                continue;
            Object object = ReflectUtil.getDeclaredFieldObj(field, null);
            if (!(object instanceof LangConfigEntry))
                continue;
            LangConfigEntry langConfigEntry = (LangConfigEntry) object;
            langConfigEntry.reset();
            langConfigWrapperMap.forEach(
                (lang, configWrapper) -> {
                    String langText = configWrapper.config().getString(langConfigEntry.key(), langConfigEntry.def());
                    langConfigEntry.setValue(lang, langText);
                }
            );
        }
    }

}
