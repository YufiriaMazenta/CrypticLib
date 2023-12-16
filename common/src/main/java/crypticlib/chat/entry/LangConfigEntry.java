package crypticlib.chat.entry;

import crypticlib.chat.LangConfigContainer;
import crypticlib.config.ConfigWrapper;
import crypticlib.util.LocaleUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public abstract class LangConfigEntry<T> {

    private final Map<String, T> langTextMap;
    private final String key;
    private final T def;

    public LangConfigEntry(@NotNull String key, T def) {
        this(key, def, new ConcurrentHashMap<>());
    }

    public LangConfigEntry(@NotNull String key, T def, @NotNull Supplier<Map<String, T>> defLangTextMapSupplier) {
        this(key, def, defLangTextMapSupplier.get());
    }

    public LangConfigEntry(@NotNull String key, T def, @NotNull Map<String, T> defLangTextMap) {
        this.key = key;
        this.langTextMap = new ConcurrentHashMap<>();
        this.def = def;
        this.langTextMap.putAll(defLangTextMap);
    }

    public LangConfigEntry<T> setValue(@NotNull Locale locale, @NotNull T value) {
        return setValue(LocaleUtil.localToLang(locale), value);
    }

    public LangConfigEntry<T> setValue(@NotNull String lang, @NotNull T value) {
        langTextMap.put(lang.toLowerCase(), value);
        return this;
    }

    public T value(@NotNull Locale locale) {
        return value(LocaleUtil.localToLang(locale));
    }

    public T value(@NotNull String lang) {
        lang = lang.toLowerCase();
        if (!langTextMap.containsKey(lang))
            return def;
        return langTextMap.get(lang);
    }

    public T value() {
        return value(Locale.getDefault());
    }

    public String key() {
        return key;
    }

    public T def() {
        return def;
    }

    public LangConfigEntry<T> load(LangConfigContainer configContainer) {
        saveDef(configContainer);
        configContainer.langConfigWrapperMap().forEach((lang, configWrapper) -> {
            langTextMap.put(lang, (T) configWrapper.config().get(key));
        });
        return this;
    }

    public void saveDef(LangConfigContainer configContainer) {
        langTextMap.forEach((lang, text) -> {
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

}
