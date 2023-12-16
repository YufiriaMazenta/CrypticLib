package crypticlib.chat;

import crypticlib.config.ConfigWrapper;
import crypticlib.util.ReflectUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LangConfigContainer {

    private final Class<?> containerClass;
    private final Map<String, ConfigWrapper> langConfigWrapperMap;
    private final File langFileFolder;

    public LangConfigContainer(@NotNull Class<?> containerClass, File langFileFolder) {
        this.langConfigWrapperMap = new ConcurrentHashMap<>();
        this.langFileFolder = langFileFolder;
        this.containerClass = containerClass;
        loadLangConfigWrapper();
    }

    private void loadLangConfigWrapper() {
        File[] langFiles = langFileFolder.listFiles();
        if (langFiles != null) {
            for (File langFile : langFiles) {
                String fileName = langFile.getName();
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
                langConfigWrapperMap.put(fileName, new ConfigWrapper(langFile));
            }
        }
    }

    public Class<?> containerClass() {
        return containerClass;
    }

    public Map<String, ConfigWrapper> langConfigWrapperMap() {
        return langConfigWrapperMap;
    }

    public File langFileFolder() {
        return langFileFolder;
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
            langConfigEntry.load(this);
        }
        langConfigWrapperMap.forEach((lang, configWrapper) -> {
            configWrapper.saveConfig();
        });
    }

    public boolean containsLang(String lang) {
        return langConfigWrapperMap.containsKey(lang);
    }

    public @Nullable ConfigWrapper getLangConfigWrapper(String lang) {
        return langConfigWrapperMap.get(lang);
    }

    public @NotNull ConfigWrapper createNewLang(String lang) {
        String fileName = lang + ".yml";
        File langFile = new File(langFileFolder, fileName);
        ConfigWrapper langConfigWrapper = new ConfigWrapper(langFile);
        langConfigWrapperMap.put(lang, langConfigWrapper);
        return langConfigWrapper;
    }

}
