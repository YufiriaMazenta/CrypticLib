package crypticlib.lang;

import crypticlib.internal.PluginScanner;
import crypticlib.lang.entry.LangEntry;
import crypticlib.lang.entry.StringLangEntry;
import crypticlib.lifecycle.AutoTask;
import crypticlib.lifecycle.BukkitLifeCycleTask;
import crypticlib.lifecycle.LifeCycle;
import crypticlib.lifecycle.TaskRule;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AutoTask(
    rules = {
        @TaskRule(lifeCycle = LifeCycle.ENABLE, priority = Integer.MIN_VALUE),
        @TaskRule(lifeCycle = LifeCycle.RELOAD),
        @TaskRule(lifeCycle = LifeCycle.DISABLE)
    }
)
public enum LangManager implements BukkitLifeCycleTask {

    INSTANCE;
    private final Map<String, Map<String, LangEntry<?>>> folderLangEntryMap = new ConcurrentHashMap<>();
    private final Map<String, LangEntryContainer> langEntryContainerMap = new ConcurrentHashMap<>();
    private final Pattern langReplacePattern = Pattern.compile("<translate:([^>]+)>");

    public void removeLangEntryContainer(@NotNull String langFileFolder) {
        LangEntryContainer container = langEntryContainerMap.remove(langFileFolder);
        if (container == null)
            return;
        folderLangEntryMap.remove(container.langFileFolder());
    }

    /**
     * 获取一个字符串类型语言对象
     * @param langFileFolder 该语言对象所属的语言文件夹
     * @param key 该语言对象的key
     */
    @Nullable
    public StringLangEntry getStringLangEntry(@NotNull String langFileFolder, @NotNull String key) {
        Map<String, LangEntry<?>> langEntryMap = folderLangEntryMap.get(langFileFolder);
        if (langEntryMap == null)
            return null;
        LangEntry<?> langEntry = langEntryMap.get(key);
        if (!(langEntry instanceof StringLangEntry)) {
            return null;
        }
        return (StringLangEntry) langEntry;
    }

    /**
     * 获取一个字符串类型语言对象,或者创建一个新的字符串语言对象
     * @param langFileFolder 该语言对象所属的语言文件夹,如果没有对应文件夹,将不会进行创建并返回null
     * @param key 该语言对象的key
     */
    @Nullable
    public StringLangEntry getStringLangEntryOrAdd(@NotNull String langFileFolder, @NotNull String key) {
        StringLangEntry stringLangEntry = getStringLangEntry(langFileFolder, key);
        if (stringLangEntry != null) {
            return stringLangEntry;
        }
        if (!langEntryContainerMap.containsKey(langFileFolder)) {
            return null;
        }
        LangEntryContainer langEntryContainer = langEntryContainerMap.get(langFileFolder);
        stringLangEntry = new StringLangEntry(key);
        langEntryContainer.addExtraStringLang(stringLangEntry);
        return stringLangEntry;
    }

    public void putLangEntry(@NotNull String langFileFolder, @NotNull LangEntry<?> entry) {
        Map<String, LangEntry<?>> langEntryMap;
        if (folderLangEntryMap.containsKey(langFileFolder)) {
            langEntryMap = folderLangEntryMap.get(langFileFolder);
        } else {
            langEntryMap = new ConcurrentHashMap<>();
            folderLangEntryMap.put(langFileFolder, langEntryMap);
        }
        langEntryMap.put(entry.key(), entry);
    }

    public void removeLangEntry(@NotNull String langFileFolder, @NotNull String key) {
        Map<String, LangEntry<?>> langEntryMap;
        if (folderLangEntryMap.containsKey(langFileFolder)) {
            langEntryMap = folderLangEntryMap.get(langFileFolder);
            langEntryMap.remove(key);
        }
    }

    /**
     * 解析一段文本里的可翻译内容,并将其替换
     * @param originStr 原始文本
     * @param sender 用于获取语言的命令发送者
     * @return 替换完成的文本
     */
    public String replaceLang(String originStr, CommandSender sender) {
        if (originStr == null) {
            return null;
        }
        Matcher matcher = langReplacePattern.matcher(originStr);
        //使用 StringBuilder 来构建替换后的字符串
        StringBuilder result = new StringBuilder();
        //用于记录当前查找到的位置
        int lastEnd = 0;

        while (matcher.find()) {
            //添加匹配项之前的文本
            result.append(originStr, lastEnd, matcher.start());

            //获取当前匹配到的翻译键key
            String langKey = matcher.group(1);

            String[] split = langKey.split(":");
            if (split.length < 2) {
                result.append(langKey);
            } else {
                //获取对应的翻译文本进行替换
                StringLangEntry stringLangEntry = getStringLangEntryOrAdd(split[0], String.join(":", Arrays.copyOfRange(split, 1, split.length)));
                //只有是StringLangEntry时,才能进行替换,如果找到的不是对应类型,直接不进行替换
                if (stringLangEntry == null) {
                    result.append(langKey);
                } else {
                    String replacement = sender instanceof Player ? stringLangEntry.value((Player) sender) : stringLangEntry.value();
                    result.append(replacement);
                }
            }

            //更新lastEnd以跳过已处理的匹配项
            lastEnd = matcher.end();
        }

        //添加剩余的文本（如果有）
        result.append(originStr.substring(lastEnd));
        return result.toString();
    }

    @Override
    public void lifecycle(Plugin plugin, LifeCycle lifeCycle) {
        switch (lifeCycle) {
            case ENABLE:
                PluginScanner.INSTANCE.getAnnotatedClasses(LangHandler.class).forEach(
                    langClass -> {
                        LangHandler langHandler = langClass.getAnnotation(LangHandler.class);
                        String langFileFolder = langHandler.langFileFolder();
                        String defLang = langHandler.defLang();
                        LangEntryContainer container = new LangEntryContainer(plugin, langClass, langFileFolder, defLang);
                        langEntryContainerMap.put(langFileFolder, container);
                        container.reload();
                    }
                );
                break;
            case RELOAD:
                folderLangEntryMap.clear();
                langEntryContainerMap.forEach(
                    (langFolder, container) -> container.reload()
                );
                break;
            case DISABLE:
                langEntryContainerMap.clear();
                folderLangEntryMap.clear();
                break;
        }
    }
}
