package crypticlib.chat.entry;

import crypticlib.chat.LangConfigContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

public class StringLangConfigEntry extends LangConfigEntry<String> {

    public StringLangConfigEntry(@NotNull String key) {
        this(key, key);
    }

    public StringLangConfigEntry(@NotNull String key, String def) {
        super(key, def);
    }

    public StringLangConfigEntry(@NotNull String key, String def, @NotNull Supplier<Map<String, String>> defLangTextMapSupplier) {
        super(key, def, defLangTextMapSupplier);
    }

    public StringLangConfigEntry(@NotNull String key, String def, @NotNull Map<String, String> defLangTextMap) {
        super(key, def, defLangTextMap);
    }

    @Override
    public StringLangConfigEntry load(LangConfigContainer configContainer) {
        saveDef(configContainer);
        langMap.clear();
        configContainer.langConfigWrapperMap().forEach((lang, configWrapper) -> {
            if (configWrapper.config().contains(key())) {
                langMap.put(lang, configWrapper.config().getString(key()));
            } else {
                configWrapper.set(key(), def());
                langMap.put(lang, def());
            }
        });
        return this;
    }

}
