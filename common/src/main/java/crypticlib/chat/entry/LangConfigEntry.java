package crypticlib.chat.entry;

import crypticlib.chat.LangConfigContainer;
import crypticlib.config.ConfigWrapper;
import crypticlib.util.LocaleUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public abstract class LangConfigEntry<T> {

    protected final Map<String, T> langMap;
    protected final Map<String, T> defLangMap;
    protected final String key;
    protected final T defValue;
    protected String defLang = "en_us";

    public LangConfigEntry(@NotNull String key, T defValue) {
        this(key, defValue, new ConcurrentHashMap<>());
    }

    public LangConfigEntry(@NotNull String key, T defValue, @NotNull Supplier<Map<String, T>> defLangMapSupplier) {
        this(key, defValue, defLangMapSupplier.get());
    }

    public LangConfigEntry(@NotNull String key, T defValue, @NotNull Map<String, T> defLangMap) {
        this.key = key;
        this.langMap = new ConcurrentHashMap<>();
        this.defValue = defValue;
        this.defLangMap = defLangMap;
    }

    public LangConfigEntry<T> setValue(@NotNull Locale locale, @NotNull T value) {
        return setValue(LocaleUtil.localToLang(locale), value);
    }

    public LangConfigEntry<T> setValue(@NotNull String lang, @NotNull T value) {
        langMap.put(lang.toLowerCase(), value);
        return this;
    }

    public T value(@NotNull Player player) {
        return value(player.getLocale());
    }

    public T value(@NotNull Locale locale) {
        return value(LocaleUtil.localToLang(locale));
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

    public abstract LangConfigEntry<T> load(LangConfigContainer configContainer);

    public void saveDef(LangConfigContainer configContainer) {
        if (defLangMap.isEmpty()) {
            configContainer.langConfigWrapperMap().forEach((lang, configWrapper) -> {
                if (!configWrapper.contains(key))
                    configWrapper.set(key, defValue);
            });
            return;
        }
        defLangMap.forEach((lang, text) -> {
            ConfigWrapper configWrapper;
            if (!configContainer.containsLang(lang)) {
                configWrapper = configContainer.createNewLang(lang);
                configWrapper.set(key, text);
            } else {
                configWrapper = configContainer.getLangConfigWrapper(lang);
                Objects.requireNonNull(configWrapper);
                if (!configWrapper.contains(key)) {
                    configWrapper.set(key, text);
                }
            }
        });
    }

    public String defLang() {
        return defLang;
    }

    public LangConfigEntry<T> setDefLang(String defLang) {
        this.defLang = defLang;
        return this;
    }

}
