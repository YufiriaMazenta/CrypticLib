package crypticlib;

import java.util.Map;
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

    public static CrypticLibPlugin plugin() {
        return crypticLibPlugin;
    }

    /**
     * 给控制台发送一条文本，此文本会处理颜色代码
     *
     * @param msg 发送的文本
     */
    public static void info(String msg) {
        plugin().msgSender().info(msg);
    }

    /**
     * 给控制台发送一条文本，此文本会处理颜色代码，并根据replaceMap的内容替换源文本
     *
     * @param msg        发送的文本
     * @param replacements 需要替换的文本
     */
    public static void info(String msg, Map<String, String> replacements) {
        plugin().msgSender().info(msg, replacements);
    }

    /**
     * 向后台发送一条DEBUG文本
     *
     * @param msg        发送的文本
     */
    public static void debug(String msg) {
        plugin().msgSender().debug(msg);
    }

    /**
     * 向后台发送一条DEBUG文本
     *
     * @param msg        发送的文本
     * @param replacements 需要替换的文本
     */
    public static void debug(String msg, Map<String, String> replacements) {
        plugin().msgSender().debug(msg, replacements);
    }

}
