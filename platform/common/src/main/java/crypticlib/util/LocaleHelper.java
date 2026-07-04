package crypticlib.util;

import java.util.Locale;

public class LocaleHelper {

    public static String local2LanguageTag(Locale locale) {
        return locale.toLanguageTag().replace("-", "_").toLowerCase();
    }

    public static Locale languageTag2Local(String languageTag) {
        return Locale.forLanguageTag(languageTag.replace("_", "-"));
    }

}
