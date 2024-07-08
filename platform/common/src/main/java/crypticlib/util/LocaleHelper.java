package crypticlib.util;

import java.util.Locale;

public class LocaleHelper {

    public static String localToLang(Locale locale) {
        return locale.toLanguageTag().replace("-", "_").toLowerCase();
    }

}
