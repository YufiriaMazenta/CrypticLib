package crypticlib.lang.entry;

import crypticlib.lang.LangEntryContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class StringListLangEntry extends LangEntry<List<String>> {

    public StringListLangEntry(@NotNull String key) {
        this(key, Collections.singletonList(key));
    }

    public StringListLangEntry(@NotNull String key, List<String> def) {
        super(key, def);
    }

    @Override
    public StringListLangEntry load(LangEntryContainer configContainer) {
        langMap.clear();
        configContainer.langConfigWrapperMap().forEach((lang, configWrapper) -> {
            if (configWrapper.contains(key)) {
                langMap.put(lang, configWrapper.config().getStringList(key));
            }
        });
        return this;
    }

}
