package crypticlib.chat.entry;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class StringListLangConfigEntry extends LangConfigEntry<List<String>> {

    public StringListLangConfigEntry(@NotNull String key, List<String> def) {
        super(key, def);
    }

    public StringListLangConfigEntry(@NotNull String key, List<String> def, @NotNull Supplier<Map<String, List<String>>> defLangTextMapSupplier) {
        super(key, def, defLangTextMapSupplier);
    }

    public StringListLangConfigEntry(@NotNull String key, List<String> def, @NotNull Map<String, List<String>> defLangTextMap) {
        super(key, def, defLangTextMap);
    }

}
