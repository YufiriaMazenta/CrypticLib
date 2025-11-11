package crypticlib;

import crypticlib.command.CommandManager;
import crypticlib.internal.CrypticLibPlugin;

import java.util.Objects;

public class CrypticLib {

    public static boolean debug = false;
    private static CrypticLibPlugin crypticLibPlugin;

    public static void init(CrypticLibPlugin plugin) {
        if (crypticLibPlugin != null) {
            throw new UnsupportedOperationException("CrypticLib is already init");
        }
        Objects.requireNonNull(plugin);
        crypticLibPlugin = plugin;
    }

    public static String pluginName() {
        return crypticLibPlugin.pluginName();
    }

    public static CommandManager<?, ?> commandManager() {
        return crypticLibPlugin.commandManager();
    }

}
