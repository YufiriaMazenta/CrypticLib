package crypticlib.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static boolean isNumber(String str) {
        str = str.trim();
        Matcher matcher = NUMBER_PATTERN.matcher(str);
        return matcher.matches();
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
