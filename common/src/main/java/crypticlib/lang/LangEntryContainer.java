package crypticlib.lang;

import com.google.common.base.Charsets;
import crypticlib.config.ConfigWrapper;
import crypticlib.lang.entry.LangEntry;
import crypticlib.util.FileUtil;
import crypticlib.util.LocaleUtil;
import crypticlib.util.ReflectUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LangEntryContainer {

    private final Class<?> containerClass;
    private final Map<String, ConfigWrapper> langConfigWrapperMap;
    private final String langFileFolder;
    private final Plugin plugin;
    private final String defLang;

    public LangEntryContainer(@NotNull Plugin plugin, @NotNull Class<?> containerClass, String langFileFolder, String defLang) {
        this.plugin = plugin;
        this.langConfigWrapperMap = new ConcurrentHashMap<>();
        this.langFileFolder = langFileFolder;
        this.containerClass = containerClass;
        this.defLang = defLang;
        saveDefLangFiles();
    }

    private void saveDefLangFiles() {
        for (Locale locale : Locale.getAvailableLocales()) {
            String lang = LocaleUtil.localToLang(locale);
            String langFileName = langFileFolder + "/" + lang + ".yml";
            if (plugin.getResource(langFileName) == null)
                continue;
            File file = new File(plugin.getDataFolder(), langFileName);
            if (!file.exists())
                plugin.saveResource(langFileName, false);
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
        loadLangFiles();
        langConfigWrapperMap.forEach((lang, configWrapper) -> configWrapper.reloadConfig());
        for (Field field : containerClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()))
                continue;
            Object object = ReflectUtil.getDeclaredFieldObj(field, null);
            if (!(object instanceof LangEntry))
                continue;
            LangEntry<?> langEntry = (LangEntry<?>) object;
            if (!langEntry.defLang().equals(defLang)) {
                langEntry.setDefLang(defLang);
            }
            langEntry.load(this);
            LangManager.INSTANCE.putLangEntry(langFileFolder, langEntry);
        }
    }

    protected void loadLangFiles() {
        langConfigWrapperMap.clear();
        File folder = new File(plugin.getDataFolder(), langFileFolder);
        //如果语言文件夹为空，防止出现NullPointerException，需要生成默认语言文件
        List<File> yamlFiles = FileUtil.allYamlFiles(folder);
        if (yamlFiles.isEmpty()) {
            saveDefLangFiles();
            yamlFiles = FileUtil.allYamlFiles(folder);
        }

        for (File langFile : yamlFiles) {
            String fileName = langFile.getName();
            String lang = fileName.substring(0, fileName.lastIndexOf("."));
            langConfigWrapperMap.put(lang, new ConfigWrapper(langFile));
        }
        updateLangFiles();
    }

    private void updateLangFiles() {
        langConfigWrapperMap.forEach(
            (lang, configWrapper) -> {
                YamlConfiguration defLangConfig = getDefLangConfig(lang);
                if (defLangConfig == null)
                    return;
                for (String key : defLangConfig.getKeys(true)) {
                    if (configWrapper.contains(key))
                        continue;
                    configWrapper.set(key, defLangConfig.get(key));
                }
                configWrapper.saveConfig();
            }
        );
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


    public YamlConfiguration getDefLangConfig(String lang) {
        String langFileName = langFileFolder + "/" + lang + ".yml";
        try(InputStream langFileInputStream = plugin.getResource(langFileName)) {
            if (langFileInputStream == null)
                return null;
            return YamlConfiguration.loadConfiguration(new InputStreamReader(langFileInputStream, Charsets.UTF_8));
        } catch (IOException e) {
            return null;
        }
    }


}
