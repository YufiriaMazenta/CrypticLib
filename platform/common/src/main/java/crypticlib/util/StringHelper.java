package crypticlib.util;

import org.jetbrains.annotations.Contract;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Map;

public class StringHelper {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    public static boolean isNumber(String text) {
        try {
            NUMBER_FORMAT.parse(text);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static Number toNumber(String text) {
        try {
            return NUMBER_FORMAT.parse(text);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String replaceStrings(String originText, Map<String, String> replacements) {
        if (replacements == null || replacements.isEmpty()) {
            return originText;
        }
        if (originText == null) {
            return null;
        }
        if (originText.isEmpty()) {
            return originText;
        }
        for (String replaceKey : replacements.keySet()) {
            originText = originText.replace(replaceKey, replacements.get(replaceKey));
        }
        return originText;
    }

}
