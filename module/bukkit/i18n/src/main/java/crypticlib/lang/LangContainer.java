package crypticlib.lang;

import crypticlib.CrypticLib;
import crypticlib.config.BukkitConfigWrapper;
import crypticlib.lang.node.LangNode;
import crypticlib.lang.node.StringLang;
import crypticlib.util.BukkitConfigHelper;
import crypticlib.util.IOHelper;
import crypticlib.util.LocaleHelper;
import crypticlib.util.ReflectionHelper;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LangContainer {

    private final Class<?> containerClass;
    private final Map<String, BukkitConfigWrapper> langConfigWrapperMap;
    /**
     * 容器类中声明的所有语言节点, 只在构造时扫描一次
     */
    private final List<LangNode<?>> langNodes;
    /**
     * 所有语言文件中出现过的键, 用于快速判断某个键是否可翻译
     */
    private final Set<String> langKeys;
    private final String langFileFolder;
    private final Plugin plugin;
    private final String defLang;
    /**
     * 额外的字符串类型语言对象
     */
    private final Map<String, StringLang> extraStringLangs = new ConcurrentHashMap<>();

    public LangContainer(@NotNull Plugin plugin, @NotNull Class<?> containerClass, String langFileFolder, String defLang) {
        this.plugin = plugin;
        this.langConfigWrapperMap = new ConcurrentHashMap<>();
        this.langNodes = new ArrayList<>();
        this.langKeys = ConcurrentHashMap.newKeySet();
        this.langFileFolder = langFileFolder;
        this.containerClass = containerClass;
        this.defLang = defLang == null ? "en_us" : defLang.toLowerCase();
        scanLangNodes();
        saveDefLangFiles();
    }

    /**
     * 释放打包在jar包中的默认语言文件夹,通常只在插件首次加载时进行
     */
    private void saveDefLangFiles() {
        File dataFolder = plugin.getDataFolder();
        for (Locale locale : Locale.getAvailableLocales()) {
            String lang = LocaleHelper.local2LanguageTag(locale);
            String langFileName = langFileFolder + "/" + lang + ".yml";
            //已释放的语言文件无需再探测jar内的资源, 探测jar是这段循环里最贵的一步
            File file = new File(dataFolder, langFileName);
            if (file.exists())
                continue;
            if (plugin.getResource(langFileName) == null)
                continue;
            plugin.saveResource(langFileName, false);
        }
    }

    /**
     * 扫描容器类中声明的静态语言节点
     * 与 ConfigContainer#scanConfigNodes 一致, 只在构造时执行一次, 重载时不再重复反射
     */
    private void scanLangNodes() {
        for (Field field : containerClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()))
                continue;
            Object object;
            try {
                object = ReflectionHelper.getDeclaredFieldObj(field, null);
            } catch (IllegalAccessException e) {
                continue;
            }
            if (!(object instanceof LangNode))
                continue;
            langNodes.add((LangNode<?>) object);
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
        for (LangNode<?> langNode : langNodes) {
            if (!defLang.equals(langNode.defLang())) {
                langNode.setDefLang(defLang);
            }
            //单个节点加载异常时记录告警并跳过, 不让一个坏键中断整个语言文件夹的加载
            try {
                langNode.load(this);
            } catch (Throwable t) {
                CrypticLib.warn("Failed to load lang value at '" + langNode.key() + "' in "
                    + langFileFolder + ": " + t.getMessage(), t);
            }
            LangManager.INSTANCE.putLangNode(langFileFolder, langNode);
        }
        extraStringLangs.forEach((lang, entry) -> {
            if (!defLang.equals(entry.defLang())) {
                entry.setDefLang(defLang);
            }
            entry.load(this);
            LangManager.INSTANCE.putLangNode(langFileFolder, entry);
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
            String lang = fileName.substring(0, fileName.lastIndexOf(".")).toLowerCase();
            //包装类构造时已读取过一次文件内容, 不再重复reload
            langConfigWrapperMap.put(lang, new BukkitConfigWrapper(langFile));
        }
        updateLangFiles();
        loadLangKeys();
    }

    /**
     * 读取插件jar内的默认语言文件并更新已经释放在插件文件夹中的语言文件,补充缺失的内容
     */
    private void updateLangFiles() {
        langConfigWrapperMap.forEach(
            (lang, configWrapper) -> {
                YamlConfiguration defLangConfig = BukkitConfigHelper.getBuiltinConfig(langFileFolder + "/" + lang + ".yml");
                if (defLangConfig == null)
                    return;
                //仅在确实补写了缺失的键时才保存, 避免每次重载都无条件重写语言文件
                boolean changed = false;
                for (String key : defLangConfig.getKeys(true)) {
                    if (configWrapper.contains(key))
                        continue;
                    configWrapper.set(key, defLangConfig.get(key));
                    changed = true;
                }
                if (changed) {
                    configWrapper.saveConfig();
                }
            }
        );
    }

    /**
     * 重建语言键索引
     * 每个语言文件的键只遍历一次, 之后判断键是否存在不必再遍历整棵配置树
     */
    private void loadLangKeys() {
        langKeys.clear();
        for (BukkitConfigWrapper configWrapper : langConfigWrapperMap.values()) {
            langKeys.addAll(configWrapper.config().getKeys(true));
        }
    }

    public boolean containsLang(String lang) {
        return langConfigWrapperMap.containsKey(lang);
    }

    /**
     * 判断该容器已加载的任意语言文件中是否存在指定 key
     * @param key 要检查的语言键
     */
    public boolean hasLangKey(@NotNull String key) {
        return langKeys.contains(key);
    }

    public @Nullable BukkitConfigWrapper getLangConfigWrapper(String lang) {
        return langConfigWrapperMap.get(lang);
    }

    public Plugin plugin() {
        return plugin;
    }

    /**
     * 添加一个额外的字符串语言对象
     * @param entry 要添加的对象
     * @return 原本存在的对象
     */
    public StringLang addExtraStringLang(StringLang entry) {
        if (!defLang.equals(entry.defLang())) {
            entry.setDefLang(defLang);
        }
        entry.load(this);
        LangManager.INSTANCE.putLangNode(langFileFolder, entry);
        return extraStringLangs.put(entry.key(), entry);
    }

    /**
     * 移除一个额外的字符串语言对象
     * @param key 要移除的额外字符串语言对象的key
     * @return 原本存在的对象
     */
    public StringLang removeExtraStringLang(String key) {
        LangManager.INSTANCE.removeLangNode(langFileFolder, key);
        return extraStringLangs.remove(key);
    }

    /**
     * 获取一个额外的字符串语言对象
     * @param key 额外字符串语言对象的key
     */
    public StringLang getExtraStringLang(String key) {
        return extraStringLangs.get(key);
    }

    /**
     * 重置额外字符串语言对象
     */
    public void clearExtraStringLangs() {
        extraStringLangs.forEach((key, entry) -> LangManager.INSTANCE.removeLangNode(langFileFolder, key));
        extraStringLangs.clear();
    }

}
