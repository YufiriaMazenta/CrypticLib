package crypticlib.util;

import java.util.Map;
import java.util.regex.Pattern;

public class StringHelper {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static boolean isNumber(String str) {
        if (str == null) return false;
        str = str.trim();
        return NUMBER_PATTERN.matcher(str).matches();
    }

    public static Double toNumber(String text) {
        if (!isNumber(text)) return null;
        return Double.parseDouble(text.trim());
    }

    public static String replaceStrings(String originText, Map<String, String> replacements) {
        if (originText == null || originText.isEmpty() || replacements == null || replacements.isEmpty()) {
            return originText;
        }
        StringBuilder sb = new StringBuilder(originText);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || key.isEmpty() || value == null) continue;
            int start = sb.indexOf(key);
            while (start != -1) {
                sb.replace(start, start + key.length(), value);
                start = sb.indexOf(key, start + value.length());
            }
        }
        return sb.toString();
    }

}
