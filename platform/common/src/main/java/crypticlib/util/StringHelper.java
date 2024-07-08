package crypticlib.util;

import java.text.NumberFormat;
import java.text.ParseException;

public class StringHelper {

    private static final NumberFormat numberFormat = NumberFormat.getInstance();

    public static boolean isNumber(String text) {
        try {
            numberFormat.parse(text);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static Number toNumber(String text) {
        try {
            return numberFormat.parse(text);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
