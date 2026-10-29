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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@LifecycleTaskConfig(
    schedules = {
        @LifecycleSchedule(phase = LifecyclePhase.LOAD, priority = Integer.MIN_VALUE),
        @LifecycleSchedule(phase = LifecyclePhase.RELOAD, priority = Integer.MIN_VALUE),
        @LifecycleSchedule(phase = LifecyclePhase.DISABLE, priority = Integer.MAX_VALUE)
    },
    platforms = PlatformSide.BUKKIT
)
public enum LangManager implements LifecycleTask {

    INSTANCE;
    /**
     * 翻译标签的前缀, 用于在正则匹配前快速排除不含翻译的文本
     */
    private static final String LANG_REPLACE_PREFIX = "<translate:";
    private static final Pattern LANG_REPLACE_PATTERN = Pattern.compile(LANG_REPLACE_PREFIX + "([^>]+)>");
    private final Map<String, Map<String, LangNode<?>>> folderLangNodeMap = new ConcurrentHashMap<>();
    private final Map<String, LangContainer> langContainerMap = new ConcurrentHashMap<>();

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
        return langNode instanceof StringLang ? (StringLang) langNode : null;
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
        LangContainer langContainer = langContainerMap.get(langFileFolder);
        if (langContainer == null) {
            return null;
        }
        stringLang = new StringLang(key);
        langContainer.addExtraStringLang(stringLang);
        return stringLang;
    }

    public void putLangNode(@NotNull String langFileFolder, @NotNull LangNode<?> entry) {
        folderLangNodeMap.computeIfAbsent(langFileFolder, k -> new ConcurrentHashMap<>()).put(entry.key(), entry);
    }

    public void removeLangNode(@NotNull String langFileFolder, @NotNull String key) {
        Map<String, LangNode<?>> langNodeMap = folderLangNodeMap.get(langFileFolder);
        if (langNodeMap != null) {
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
        //绝大多数文本不含翻译标签, 先做一次子串查找, 未命中时直接返回原文本, 省去正则匹配与字符串构建
        if (originStr.indexOf(LANG_REPLACE_PREFIX) < 0) {
            return originStr;
        }
        Matcher matcher = LANG_REPLACE_PATTERN.matcher(originStr);
        //使用 StringBuilder 来构建替换后的字符串
        StringBuilder result = new StringBuilder(originStr.length());
        //用于记录当前查找到的位置
        int lastEnd = 0;
        //玩家语言只在本次替换中取一次, 避免每个标签都判断一次发送者类型
        Player player = sender instanceof Player ? (Player) sender : null;

        while (matcher.find()) {
            //添加匹配项之前的文本
            result.append(originStr, lastEnd, matcher.start());
            //添加翻译后的文本, 无法翻译时原样保留key
            result.append(translate(matcher.group(1), player));
            //更新lastEnd以跳过已处理的匹配项
            lastEnd = matcher.end();
        }

        //添加剩余的文本（如果有）
        result.append(originStr, lastEnd, originStr.length());
        return result.toString();
    }

    /**
     * 翻译单个翻译键
     * @param langKey 形如 folder:key 的翻译键
     * @param player 用于获取语言的玩家, 为null时使用默认语言
     * @return 翻译后的文本, 无法翻译时返回原始翻译键
     */
    @NotNull
    private String translate(@NotNull String langKey, @Nullable Player player) {
        //只按第一个冒号切分, 避免 split + 数组拷贝 + 重新拼接的三次额外分配
        int splitIndex = langKey.indexOf(':');
        if (splitIndex < 0) {
            return langKey;
        }
        String folder = langKey.substring(0, splitIndex);
        String key = langKey.substring(splitIndex + 1);
        StringLang stringLang = getStringLang(folder, key);
        if (stringLang == null) {
            //先只读查找已注册条目; 未命中时, 仅当该key确实存在于语言文件中才注册加载,
            //既支持用户在语言文件中自定义新key, 又避免为任意未知key(可能来自玩家输入)永久注册条目导致内存无界增长
            LangContainer container = langContainerMap.get(folder);
            if (container == null || !container.hasLangKey(key)) {
                return langKey;
            }
            stringLang = new StringLang(key);
            container.addExtraStringLang(stringLang);
        }
        return player == null ? stringLang.value() : stringLang.value(player);
    }

    @Override
    public void onLifecycle(CrypticLibPlugin plugin, LifecyclePhase lifecyclePhase) {
        switch (lifecyclePhase) {
            case LOAD:
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
