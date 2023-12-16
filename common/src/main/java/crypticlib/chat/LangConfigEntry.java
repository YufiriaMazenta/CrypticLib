package crypticlib.chat;

import crypticlib.config.ConfigWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class LangConfigEntry {

    private final Map<String, String> langTextMap;
    private final String key;
    private final String defText;

    public LangConfigEntry(@NotNull String key, String defText) {
        this(key, defText, new ConcurrentHashMap<>());
    }

    public LangConfigEntry(@NotNull String key, String defText, @NotNull Supplier<Map<String, String>> defLangTextMapSupplier) {
        this(key, defText, defLangTextMapSupplier.get());
    }

    public LangConfigEntry(@NotNull String key, String defText, @NotNull Map<String, String> defLangTextMap) {
        this.key = key;
        this.langTextMap = new ConcurrentHashMap<>();
        this.defText = defText;
        this.langTextMap.putAll(defLangTextMap);
    }

    public LangConfigEntry setValue(@NotNull Locale locale, @NotNull String value) {
        return setValue(localToLang(locale), value);
    }

    public LangConfigEntry setValue(@NotNull String lang, @NotNull String value) {
        langTextMap.put(lang.toLowerCase(), value);
        return this;
    }

    public @NotNull String value(@NotNull Locale locale) {
        return value(localToLang(locale));
    }

    public @NotNull String value(@NotNull String lang) {
        lang = lang.toLowerCase();
        if (!langTextMap.containsKey(lang))
            return defText;
        return langTextMap.get(lang);
    }

    public @NotNull String value() {
        return value(Locale.getDefault());
    }

    public String key() {
        return key;
    }

    public String def() {
        return defText;
    }

    public LangConfigEntry load(LangConfigContainer configContainer) {
        saveDef(configContainer);
        configContainer.langConfigWrapperMap().forEach((lang, configWrapper) -> {
            langTextMap.put(lang, configWrapper.config().getString(key));
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

    private String localToLang(Locale locale) {
        return locale.toLanguageTag().replace("-", "_");
    }

}
