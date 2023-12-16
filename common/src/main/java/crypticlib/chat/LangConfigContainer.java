package crypticlib.chat;

import crypticlib.chat.entry.LangConfigEntry;
import crypticlib.config.ConfigWrapper;
import crypticlib.util.LocaleUtil;
import crypticlib.util.ReflectUtil;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LangConfigContainer {

    private final Class<?> containerClass;
    private final Map<String, ConfigWrapper> langConfigWrapperMap;
    private final String langFileFolder;
    private final Plugin plugin;
    private String defLang;

    public LangConfigContainer(@NotNull Plugin plugin, @NotNull Class<?> containerClass, String langFileFolder, String defLang) {
        this.plugin = plugin;
        this.langConfigWrapperMap = new ConcurrentHashMap<>();
        this.langFileFolder = langFileFolder;
        this.containerClass = containerClass;
        this.defLang = defLang;
        loadLangConfigWrapper();
    }

    private void loadLangConfigWrapper() {
        for (Locale locale : Locale.getAvailableLocales()) {
            String lang = LocaleUtil.localToLang(locale);
            String langFileName = langFileFolder + "/" + lang + ".yml";
            if (plugin.getResource(langFileName) == null)
                continue;
            File file = new File(plugin.getDataFolder(), langFileName);
            if (!file.exists())
                plugin.saveResource(langFileName, false);
        }
        File folder = new File(plugin.getDataFolder(), langFileFolder);

        File[] langFiles = folder.listFiles();
        if (langFiles != null) {
            for (File langFile : langFiles) {
                String fileName = langFile.getName();
                String lang = fileName.substring(0, fileName.lastIndexOf("."));
                langConfigWrapperMap.put(lang, new ConfigWrapper(langFile));
            }
        }
    }

    public Class<?> containerClass() {
        return containerClass;
    }

    public Map<String, ConfigWrapper> langConfigWrapperMap() {
        return langConfigWrapperMap;
    }

    public String langFileFolder() {
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
            LangConfigEntry<?> langConfigEntry = (LangConfigEntry<?>) object;
            if (!langConfigEntry.defLang().equals(defLang)) {
                langConfigEntry.setDefLang(defLang);
            }
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
        ConfigWrapper langConfigWrapper = new ConfigWrapper(plugin, langFileFolder + "/" + fileName);
        langConfigWrapperMap.put(lang, langConfigWrapper);
        return langConfigWrapper;
    }

    public Plugin plugin() {
        return plugin;
    }

}
