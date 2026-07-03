package crypticlib;

import crypticlib.chat.MsgSender;
import crypticlib.command.CommandManager;
import crypticlib.internal.CrypticLibPlugin;
import crypticlib.scheduler.Scheduler;

import java.util.Objects;

public class CrypticLib {

    public static boolean debug = false;
    private static CrypticLibPlugin crypticLibPlugin;
    public static final PlatformSide CURRENT_PLATFORM;

    static {
        if (classExists("org.bukkit.Bukkit")) {
            CURRENT_PLATFORM = PlatformSide.BUKKIT;
        } else if (classExists("net.md_5.bungee.api.ProxyServer")) {
            CURRENT_PLATFORM = PlatformSide.BUNGEE;
        } else if (classExists("com.velocitypowered.api.proxy.ProxyServer")) {
            CURRENT_PLATFORM = PlatformSide.VELOCITY;
        } else {
            throw new IllegalStateException("Unknown platform");
        }
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

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

    public static MsgSender msgSender() {
        return crypticLibPlugin.msgSender();
    }

    public static Scheduler scheduler() {
        return crypticLibPlugin.scheduler();
    }

}
