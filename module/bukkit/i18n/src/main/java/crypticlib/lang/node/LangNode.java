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
        langMap.put(lang.toLowerCase(), value);
        return this;
    }

    public T value(@NotNull Player player) {
        return value(player.getLocale());
    }

    public T value(@NotNull Locale locale) {
        return value(LocaleHelper.local2LanguageTag(locale));
    }

    public T value(@NotNull String lang) {
        lang = lang.toLowerCase();
        if (langMap.containsKey(lang))
            return langMap.get(lang);
        if (langMap.containsKey(defLang))
            return langMap.get(defLang);
        return defValue;
    }

    public T value() {
        return value(Locale.getDefault());
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
        this.defLang = defLang;
        return this;
    }

    public void send(CommandSender sender) {
        send(sender, Collections.emptyMap());
    }

    public abstract void send(CommandSender sender, Map<String, String> replaceMap);

}
