package crypticlib.chat;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class LangConfigEntry {

    private final Map<String, String> langTextMap;
    private final String key;
    private final String def;

    public LangConfigEntry(@NotNull String key, String def) {
        this(key, def, new ConcurrentHashMap<>());
    }

    public LangConfigEntry(@NotNull String key, String def, @NotNull Supplier<Map<String, String>> langMapSupplier) {
        this(key, def, langMapSupplier.get());
    }

    public LangConfigEntry(@NotNull String key, String def, @NotNull Map<String, String> langTextMap) {
        this.key = key;
        this.langTextMap = new ConcurrentHashMap<>();
        this.def = def;
        this.langTextMap.putAll(langTextMap);
    }

    public LangConfigEntry setValue(@NotNull String lang, @NotNull String value) {
        langTextMap.put(lang, value);
        return this;
    }

    public @NotNull String value(@NotNull String lang) {
        if (!langTextMap.containsKey(lang))
            return def;
        return langTextMap.get(lang);
    }

    public @NotNull String value() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        String region = locale.getCountry();
        if (!region.isEmpty())
            language = language + "-" + region;
        return value(language);
    }

    public String key() {
        return key;
    }

    public String def() {
        return def;
    }

    public LangConfigEntry reset() {
        langTextMap.clear();
        return this;
    }

}
