package crypticlib.lang.node;

import crypticlib.lang.LangContainer;
import crypticlib.util.LocaleHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class LangNode<T> {

    protected final Map<String, T> langMap;
    protected final String key;
    protected T defValue;
    protected String defLang = "en_us";

    public LangNode(@NotNull String key, T defValue) {
        this.key = key;
        this.defValue = defValue;
        this.langMap = new ConcurrentHashMap<>();
    }

    public LangNode<T> setValue(@NotNull Locale locale, @NotNull T value) {
        return setValue(LocaleHelper.local2LanguageTag(locale), value);
    }

    public LangNode<T> setValue(@NotNull String lang, @NotNull T value) {
        langMap.put(normalizeLang(lang), value);
        return this;
    }

    public T value(@NotNull Player player) {
        return value(player.getLocale());
    }

    public T value(@NotNull Locale locale) {
        return value(LocaleHelper.local2LanguageTag(locale));
    }

    public T value(@NotNull String lang) {
        //绝大多数调用方传入的已是小写语言标签, 因此先按原样查一次, 命中时无需任何额外对象
        T value = langMap.get(lang);
        if (value != null)
            return value;
        String normalizedLang = normalizeLang(lang);
        if (!normalizedLang.equals(lang)) {
            value = langMap.get(normalizedLang);
            if (value != null)
                return value;
        }
        if (defLang != null) {
            value = langMap.get(defLang);
            if (value != null)
                return value;
        }
        return defValue;
    }

    public T value() {
        return value(Locale.getDefault());
    }

    /**
     * 统一语言标签的大小写, 与语言文件名的处理方式保持一致
     * @param lang 原始语言标签
     * @return 小写形式的语言标签, 本身就是小写时直接返回原对象
     */
    @NotNull
    protected static String normalizeLang(@NotNull String lang) {
        return lang.toLowerCase(Locale.ROOT);
    }

    public String key() {
        return key;
    }

    public T def() {
        return defValue;
    }

    public abstract LangNode<T> load(LangContainer configContainer);

    public String defLang() {
        return defLang;
    }

    public LangNode<T> setDefLang(String defLang) {
        this.defLang = defLang == null ? null : normalizeLang(defLang);
        return this;
    }

    public void send(CommandSender sender) {
        send(sender, Collections.emptyMap());
    }

    public abstract void send(CommandSender sender, Map<String, String> replaceMap);

}
