package crypticlib;

import crypticlib.command.CommandManager;

public class CrypticLib {

    private static boolean debug = false;
    private static String pluginName;
    private static CommandManager<?, ?> commandManager;

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

    public static String pluginName() {
        return pluginName;
    }

    public static void setPluginName(String pluginName) {
        CrypticLib.pluginName = pluginName;
    }

    public static CommandManager<?, ?> commandManager() {
        return commandManager;
    }

    public static void setCommandManager(CommandManager<?, ?> commandManager) {
        CrypticLib.commandManager = commandManager;
    }

}
