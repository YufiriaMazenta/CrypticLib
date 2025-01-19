package crypticlib;

public class CrypticLib {

    private static boolean debug = false;

    /**
     * 获取是否启用了调试模式
     * @return 是否启用了调试模式
     */
    public static boolean debug() {
        return debug;
    }

    /**
     * 设置是否启用调试模式
     * @param debug 是否启用debug
     */
    public static void setDebug(boolean debug) {
        CrypticLib.debug = debug;
    }

}
