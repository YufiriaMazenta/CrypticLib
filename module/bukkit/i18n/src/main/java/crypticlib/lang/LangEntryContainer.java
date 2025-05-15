package crypticlib.lang;

import com.google.common.base.Charsets;
import crypticlib.config.BukkitConfigWrapper;
import crypticlib.lang.entry.LangEntry;
import crypticlib.lang.entry.StringLangEntry;
import crypticlib.util.IOHelper;
import crypticlib.util.LocaleHelper;
import crypticlib.util.ReflectionHelper;
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
    private final Map<String, BukkitConfigWrapper> langConfigWrapperMap;
    private final String langFileFolder;
    private final Plugin plugin;
    private final String defLang;
    /**
     * 额外的字符串类型语言对象
     */
    private final Map<String, StringLangEntry> extraStringLangEntries = new ConcurrentHashMap<>();

    public LangEntryContainer(@NotNull Plugin plugin, @NotNull Class<?> containerClass, String langFileFolder, String defLang) {
        this.plugin = plugin;
        this.langConfigWrapperMap = new ConcurrentHashMap<>();
        this.langFileFolder = langFileFolder;
        this.containerClass = containerClass;
        this.defLang = defLang;
        saveDefLangFiles();
    }

    /**
     * 释放打包在jar包中的默认语言文件夹,通常只在插件首次加载时进行
     */
    private void saveDefLangFiles() {
        for (Locale locale : Locale.getAvailableLocales()) {
            String lang = LocaleHelper.localToLang(locale);
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

    public Map<String, BukkitConfigWrapper> langConfigWrapperMap() {
        return langConfigWrapperMap;
    }

    public String langFileFolder() {
        return langFileFolder;
    }

    /**
     * 重新加载所有语言文件和语言对象
     */
    public void reload() {
        loadLangFiles();
        langConfigWrapperMap.forEach((lang, configWrapper) -> configWrapper.reloadConfig());
        for (Field field : containerClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()))
                continue;
            Object object = ReflectionHelper.getDeclaredFieldObj(field, null);
            if (!(object instanceof LangEntry))
                continue;
            LangEntry<?> langEntry = (LangEntry<?>) object;
            if (!langEntry.defLang().equals(defLang)) {
                langEntry.setDefLang(defLang);
            }
            langEntry.load(this);
            LangManager.INSTANCE.putLangEntry(langFileFolder, langEntry);
        }
        extraStringLangEntries.forEach((lang, entry) -> {
            if (!entry.defLang().equals(defLang)) {
                entry.setDefLang(defLang);
            }
            entry.load(this);
        });
    }

    /**
     * 加载所有的语言文件
     * 流程如下:读取语言文件夹中的文件->释放
     */
    protected void loadLangFiles() {
        langConfigWrapperMap.clear();
        File folder = new File(plugin.getDataFolder(), langFileFolder);
        //如果语言文件夹为空，防止出现NullPointerException，需要生成默认语言文件
        List<File> yamlFiles = IOHelper.allYamlFiles(folder);
        if (yamlFiles.isEmpty()) {
            saveDefLangFiles();
            yamlFiles = IOHelper.allYamlFiles(folder);
        }

        for (File langFile : yamlFiles) {
            String fileName = langFile.getName();
            String lang = fileName.substring(0, fileName.lastIndexOf("."));
            BukkitConfigWrapper configWrapper = new BukkitConfigWrapper(langFile);
            configWrapper.reloadConfig();
            langConfigWrapperMap.put(lang, configWrapper);
        }
        updateLangFiles();
    }

    /**
     * 读取插件jar内的默认语言文件并更新已经释放在插件文件夹中的语言文件,补充缺失的内容
     */
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

    public @Nullable BukkitConfigWrapper getLangConfigWrapper(String lang) {
        return langConfigWrapperMap.get(lang);
    }

    /**
     * 创建一个新的语言文件
     * 其实没什么用,因为语言文件内容需要自己补充
     */
    public @NotNull BukkitConfigWrapper createNewLang(String lang) {
        String fileName = lang + ".yml";
        BukkitConfigWrapper langConfigWrapper = new BukkitConfigWrapper(plugin, langFileFolder + "/" + fileName);
        langConfigWrapperMap.put(lang, langConfigWrapper);
        return langConfigWrapper;
    }

    public Plugin plugin() {
        return plugin;
    }

    /**
     * 获取打包在插件jar内的对应语言的默认语言文件
     * @param lang 要获取的语言
     * @return
     */
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

    /**
     * 添加一个额外的字符串语言对象
     * @param entry 要添加的对象
     * @return 原本存在的对象
     */
    public StringLangEntry addExtraStringLang(StringLangEntry entry) {
        if (!entry.defLang().equals(defLang)) {
            entry.setDefLang(defLang);
        }
        entry.load(this);
        LangManager.INSTANCE.putLangEntry(langFileFolder, entry);
        return extraStringLangEntries.put(entry.key(), entry);
    }

    /**
     * 移除一个额外的字符串语言对象
     * @param key 要移除的额外字符串语言对象的key
     * @return 原本存在的对象
     */
    public StringLangEntry removeExtraStringLang(String key) {
        LangManager.INSTANCE.removeLangEntry(langFileFolder, key);
        return extraStringLangEntries.remove(key);
    }

    /**
     * 获取一个额外的字符串语言对象
     * @param key 额外字符串语言对象的key
     */
    public StringLangEntry getExtraStringLang(String key) {
        return extraStringLangEntries.get(key);
    }

    /**
     * 重置额外字符串语言对象
     */
    public void clearExtraStringLangEntries() {
        extraStringLangEntries.forEach((key, entry) -> LangManager.INSTANCE.removeLangEntry(langFileFolder, key));
        extraStringLangEntries.clear();
    }

}
