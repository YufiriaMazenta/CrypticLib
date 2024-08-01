package crypticlib.lang.entry;

import crypticlib.chat.BukkitMsgSender;
import crypticlib.lang.LangEntryContainer;
import crypticlib.util.LocaleHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class LangEntry<T> {

    protected final Map<String, T> langMap;
    protected final String key;
    protected T defValue;
    protected String defLang = "en_us";

    public LangEntry(@NotNull String key, T defValue) {
        this.key = key;
        this.defValue = defValue;
        this.langMap = new ConcurrentHashMap<>();
    }

    public LangEntry<T> setValue(@NotNull Locale locale, @NotNull T value) {
        return setValue(LocaleHelper.localToLang(locale), value);
    }

    public LangEntry<T> setValue(@NotNull String lang, @NotNull T value) {
        langMap.put(lang.toLowerCase(), value);
        return this;
    }

    public T value(@NotNull Player player) {
        return value(player.getLocale());
    }

    public T value(@NotNull Locale locale) {
        return value(LocaleHelper.localToLang(locale));
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

    public abstract LangEntry<T> load(LangEntryContainer configContainer);

    public String defLang() {
        return defLang;
    }

    public LangEntry<T> setDefLang(String defLang) {
        this.defLang = defLang;
        return this;
    }

    public abstract void send(CommandSender sender);

}
