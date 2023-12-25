package crypticlib.chat.entry;

import crypticlib.chat.LangConfigContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class StringListLangConfigEntry extends LangConfigEntry<List<String>> {

    public StringListLangConfigEntry(@NotNull String key) {
        this(key, Collections.singletonList(key));
    }

    public StringListLangConfigEntry(@NotNull String key, List<String> def) {
        super(key, def);
    }

    @Override
    public StringListLangConfigEntry load(LangConfigContainer configContainer) {
        langMap.clear();
        configContainer.langConfigWrapperMap().forEach((lang, configWrapper) -> {
            if (configWrapper.contains(key)) {
                langMap.put(lang, configWrapper.config().getStringList(key));
            }
        });
        return this;
    }

}
