package crypticlib.lang;

import crypticlib.CrypticLibPlugin;
import crypticlib.PlatformSide;
import crypticlib.internal.PluginScanner;
import crypticlib.lang.node.LangNode;
import crypticlib.lang.node.StringLang;
import crypticlib.lifecycle.LifecycleTaskConfig;
import crypticlib.lifecycle.LifecycleTask;
import crypticlib.lifecycle.LifecyclePhase;
import crypticlib.lifecycle.LifecycleSchedule;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@LifecycleTaskConfig(
    schedules = {
        @LifecycleSchedule(phase = LifecyclePhase.ENABLE, priority = Integer.MIN_VALUE),
        @LifecycleSchedule(phase = LifecyclePhase.RELOAD, priority = Integer.MIN_VALUE, isAsync = true),
        @LifecycleSchedule(phase = LifecyclePhase.DISABLE, priority = Integer.MAX_VALUE)
    },
    platforms = PlatformSide.BUKKIT
)
public enum LangManager implements LifecycleTask {

    INSTANCE;
    private final Map<String, Map<String, LangNode<?>>> folderLangNodeMap = new ConcurrentHashMap<>();
    private final Map<String, LangContainer> langContainerMap = new ConcurrentHashMap<>();
    private final Pattern langReplacePattern = Pattern.compile("<translate:([^>]+)>");

    public void removeLangContainer(@NotNull String langFileFolder) {
        LangContainer container = langContainerMap.remove(langFileFolder);
        if (container == null)
            return;
        folderLangNodeMap.remove(container.langFileFolder());
    }

    /**
     * 获取一个字符串类型语言对象
     * @param langFileFolder 该语言对象所属的语言文件夹
     * @param key 该语言对象的key
     */
    @Nullable
    public StringLang getStringLang(@NotNull String langFileFolder, @NotNull String key) {
        Map<String, LangNode<?>> langNodeMap = folderLangNodeMap.get(langFileFolder);
        if (langNodeMap == null)
            return null;
        LangNode<?> langNode = langNodeMap.get(key);
        if (!(langNode instanceof StringLang)) {
            return null;
        }
        return (StringLang) langNode;
    }

    /**
     * 获取一个字符串类型语言对象,或者创建一个新的字符串语言对象
     * @param langFileFolder 该语言对象所属的语言文件夹,如果没有对应文件夹,将不会进行创建并返回null
     * @param key 该语言对象的key
     */
    @Nullable
    public StringLang getStringLangOrAdd(@NotNull String langFileFolder, @NotNull String key) {
        StringLang stringLang = getStringLang(langFileFolder, key);
        if (stringLang != null) {
            return stringLang;
        }
        if (!langContainerMap.containsKey(langFileFolder)) {
            return null;
        }
        LangContainer langContainer = langContainerMap.get(langFileFolder);
        stringLang = new StringLang(key);
        langContainer.addExtraStringLang(stringLang);
        return stringLang;
    }

    public void putLangNode(@NotNull String langFileFolder, @NotNull LangNode<?> entry) {
        Map<String, LangNode<?>> langNodeMap;
        if (folderLangNodeMap.containsKey(langFileFolder)) {
            langNodeMap = folderLangNodeMap.get(langFileFolder);
        } else {
            langNodeMap = new ConcurrentHashMap<>();
            folderLangNodeMap.put(langFileFolder, langNodeMap);
        }
        langNodeMap.put(entry.key(), entry);
    }

    public void removeLangNode(@NotNull String langFileFolder, @NotNull String key) {
        Map<String, LangNode<?>> langNodeMap;
        if (folderLangNodeMap.containsKey(langFileFolder)) {
            langNodeMap = folderLangNodeMap.get(langFileFolder);
            langNodeMap.remove(key);
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
                String folder = split[0];
                String key = String.join(":", Arrays.copyOfRange(split, 1, split.length));
                //先只读查找已注册条目; 未命中时, 仅当该key确实存在于语言文件中才注册加载,
                //既支持用户在语言文件中自定义新key, 又避免为任意未知key(可能来自玩家输入)永久注册条目导致内存无界增长
                StringLang stringLang = getStringLang(folder, key);
                if (stringLang == null) {
                    LangContainer container = langContainerMap.get(folder);
                    if (container != null && container.hasLangKey(key)) {
                        stringLang = getStringLangOrAdd(folder, key);
                    }
                }
                //只有是StringLang时,才能进行替换,如果找到的不是对应类型,直接不进行替换
                if (stringLang == null) {
                    result.append(langKey);
                } else {
                    String replacement = sender instanceof Player ? stringLang.value((Player) sender) : stringLang.value();
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
    public void onLifecycle(CrypticLibPlugin plugin, LifecyclePhase lifecyclePhase) {
        switch (lifecyclePhase) {
            case ENABLE:
                Plugin bukkitPlugin = (Plugin) plugin;
                PluginScanner.INSTANCE.getAnnotatedClasses(LangHandler.class).forEach(
                    langClass -> {
                        LangHandler langHandler = langClass.getAnnotation(LangHandler.class);
                        String langFileFolder = langHandler.langFileFolder();
                        String defLang = langHandler.defLang();
                        LangContainer container = new LangContainer(bukkitPlugin, langClass, langFileFolder, defLang);
                        langContainerMap.put(langFileFolder, container);
                        container.reload();
                    }
                );
                break;
            case RELOAD:
                folderLangNodeMap.clear();
                langContainerMap.forEach(
                    (langFolder, container) -> container.reload()
                );
                break;
            case DISABLE:
                langContainerMap.clear();
                folderLangNodeMap.clear();
                break;
        }
    }
}
