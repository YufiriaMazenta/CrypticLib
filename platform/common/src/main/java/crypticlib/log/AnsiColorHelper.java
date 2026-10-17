package crypticlib.log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将原版颜色代码转换为 ANSI 转义序列，供平台原生 logger 显示颜色使用
 */
public final class AnsiColorHelper {

    /**
     * ANSI 重置转义序列
     */
    public static final String RESET = "\u001B[0m";

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})");

    /**
     * 将文本中的原版颜色代码转换为 ANSI 转义序列
     *
     * @param text 原始文本
     * @return 转换后的文本
     */
    public static String translate(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        text = translateHex(text);
        StringBuilder out = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            //同时兼容 '&' 与 '§' 两种引导符
            if ((ch == '&' || ch == '§') && i + 1 < text.length()) {
                String ansi = ansiOf(Character.toLowerCase(text.charAt(i + 1)));
                if (ansi != null) {
                    out.append(ansi);
                    i++;
                    continue;
                }
            }
            out.append(ch);
        }
        return out.toString();
    }

    private static String translateHex(String text) {
        if (!text.contains("&#")) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text);
        Matcher matcher = HEX_PATTERN.matcher(sb);
        int searchFrom = 0;
        while (matcher.find(searchFrom)) {
            String colorCode = matcher.group();
            String hex = matcher.group(1);
            //将CSS风格的3位hex(如abc)按规则展开为6位(aabbcc)
            if (hex.length() == 3) {
                hex = String.valueOf(hex.charAt(0)) + hex.charAt(0) +
                    hex.charAt(1) + hex.charAt(1) +
                    hex.charAt(2) + hex.charAt(2);
            }
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            String ansi = RESET + "\u001B[38;2;" + r + ";" + g + ";" + b + "m";
            sb.replace(matcher.start(), matcher.start() + colorCode.length(), ansi);
            searchFrom = matcher.start() + ansi.length();
            matcher = HEX_PATTERN.matcher(sb);
        }
        return sb.toString();
    }

    /**
     * 颜色代码字符对应的 ANSI 转义序列，非颜色代码返回 null
     */
    private static String ansiOf(char code) {
        switch (code) {
            case '0':
                return RESET + "\u001B[30m";
            case '1':
                return RESET + "\u001B[34m";
            case '2':
                return RESET + "\u001B[32m";
            case '3':
                return RESET + "\u001B[36m";
            case '4':
                return RESET + "\u001B[31m";
            case '5':
                return RESET + "\u001B[35m";
            case '6':
                return RESET + "\u001B[33m";
            case '7':
                return RESET + "\u001B[37m";
            case '8':
                return RESET + "\u001B[90m";
            case '9':
                return RESET + "\u001B[94m";
            case 'a':
                return RESET + "\u001B[92m";
            case 'b':
                return RESET + "\u001B[96m";
            case 'c':
                return RESET + "\u001B[91m";
            case 'd':
                return RESET + "\u001B[95m";
            case 'e':
                return RESET + "\u001B[93m";
            case 'f':
                return RESET + "\u001B[97m";
            case 'l':
                return "\u001B[1m";
            case 'm':
                return "\u001B[9m";
            case 'n':
                return "\u001B[4m";
            case 'o':
                return "\u001B[3m";
            case 'r':
                return RESET;
            //'k'(乱码)不支持用 ANSI 表达，忽略
            default:
                return null;
        }
    }

}
