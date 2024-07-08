package crypticlib.lang.entry;

import crypticlib.lang.LangEntryContainer;
import org.jetbrains.annotations.NotNull;

public class StringLangEntry extends LangEntry<String> {

    public StringLangEntry(@NotNull String key) {
        this(key, key);
    }

    public StringLangEntry(@NotNull String key, String def) {
        super(key, def);
    }

    @Override
    public StringLangEntry load(LangEntryContainer configContainer) {
        langMap.clear();
        configContainer.langConfigWrapperMap().forEach((lang, configWrapper) -> {
            if (configWrapper.contains(key)) {
                langMap.put(lang, configWrapper.config().getString(key));
            }
        });
        return this;
    }

}
