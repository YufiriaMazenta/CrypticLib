package crypticlib.chat.entry;

import crypticlib.chat.LangConfigContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class StringListLangConfigEntry extends LangConfigEntry<List<String>> {

    public StringListLangConfigEntry(@NotNull String key) {
        this(key, Collections.singletonList(key));
    }

    public StringListLangConfigEntry(@NotNull String key, List<String> def) {
        super(key, def);
    }

    public StringListLangConfigEntry(@NotNull String key, List<String> def, @NotNull Supplier<Map<String, List<String>>> defLangTextMapSupplier) {
        super(key, def, defLangTextMapSupplier);
    }

    public StringListLangConfigEntry(@NotNull String key, List<String> def, @NotNull Map<String, List<String>> defLangTextMap) {
        super(key, def, defLangTextMap);
    }

    @Override
    public StringListLangConfigEntry load(LangConfigContainer configContainer) {
        save(configContainer);
        configContainer.langConfigWrapperMap().forEach((lang, configWrapper) -> {
            if (configWrapper.config().contains(key())) {
                langMap.put(lang, configWrapper.config().getStringList(key()));
            } else {
                configWrapper.set(key(), def());
                langMap.put(lang, def());
            }
        });
        return this;
    }

}
