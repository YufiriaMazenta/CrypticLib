package crypticlib.chat.entry;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

public class StringLangConfigEntry extends LangConfigEntry<String> {

    public StringLangConfigEntry(@NotNull String key, String def) {
        super(key, def);
    }

    public StringLangConfigEntry(@NotNull String key, String def, @NotNull Supplier<Map<String, String>> defLangTextMapSupplier) {
        super(key, def, defLangTextMapSupplier);
    }

    public StringLangConfigEntry(@NotNull String key, String def, @NotNull Map<String, String> defLangTextMap) {
        super(key, def, defLangTextMap);
    }

}
