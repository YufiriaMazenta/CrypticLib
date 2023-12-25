package crypticlib.chat.entry;

import crypticlib.chat.LangConfigContainer;
import org.jetbrains.annotations.NotNull;

public class StringLangConfigEntry extends LangConfigEntry<String> {

    public StringLangConfigEntry(@NotNull String key) {
        this(key, key);
    }

    public StringLangConfigEntry(@NotNull String key, String def) {
        super(key, def);
    }

    @Override
    public StringLangConfigEntry load(LangConfigContainer configContainer) {
        langMap.clear();
        configContainer.langConfigWrapperMap().forEach((lang, configWrapper) -> {
            if (configWrapper.contains(key)) {
                langMap.put(lang, configWrapper.config().getString(key));
            } else {
                configWrapper.set(key, defValue);
                langMap.put(lang, defValue);
            }
        });
        return this;
    }

}
