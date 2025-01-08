package crypticlib.util;

import java.text.NumberFormat;
import java.text.ParseException;

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

}
