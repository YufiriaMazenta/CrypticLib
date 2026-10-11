package crypticlib;

import crypticlib.log.LogLevel;
import crypticlib.log.Logger;
import crypticlib.log.MsgSenderLogger;

import java.util.Map;
import java.util.Objects;

public class CrypticLib {

    private static CrypticLibPlugin crypticLibPlugin;
    private static Logger LOGGER;
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
        LOGGER = new MsgSenderLogger(plugin.msgSender(), LogLevel.INFO);
    }

    public static CrypticLibPlugin plugin() {
        return crypticLibPlugin;
    }

    /**
     * @return 全局日志器，可设置日志级别并输出 verbose/info/warn/error 等日志。
     * <p>打开调试日志：{@code CrypticLib.logger().setLevel(LogLevel.DEBUG)}（默认阈值为 {@link LogLevel#INFO}）。
     */
    public static Logger logger() {
        return LOGGER;
    }

    /**
     * 输出一条指定级别的日志
     *
     * @param level 日志级别
     * @param msg   发送的文本
     */
    public static void log(LogLevel level, String msg) {
        LOGGER.log(level, msg);
    }

    /**
     * 输出一条指定级别的日志，并根据 replacements 的内容替换源文本
     *
     * @param level        日志级别
     * @param msg          发送的文本
     * @param replacements 需要替换的文本
     */
    public static void log(LogLevel level, String msg, Map<String, String> replacements) {
        LOGGER.log(level, msg, replacements);
    }

    /**
     * 给控制台发送一条“简单日志”(INFO)，此文本会处理颜色代码。
     *
     * @param msg 发送的文本
     */
    public static void info(String msg) {
        LOGGER.info(msg);
    }

    /**
     * 给控制台发送一条“简单日志”(INFO)，此文本会处理颜色代码，并根据replaceMap的内容替换源文本
     *
     * @param msg        发送的文本
     * @param replacements 需要替换的文本
     */
    public static void info(String msg, Map<String, String> replacements) {
        LOGGER.info(msg, replacements);
    }

    /**
     * 给控制台发送一条“详细日志”(VERBOSE)，仅当阈值不高于 {@link LogLevel#VERBOSE} 时输出
     *
     * @param msg 发送的文本
     */
    public static void verbose(String msg) {
        LOGGER.verbose(msg);
    }

    /**
     * 给控制台发送一条“详细日志”(VERBOSE)，仅当阈值不高于 {@link LogLevel#VERBOSE} 时输出
     *
     * @param msg        发送的文本
     * @param replacements 需要替换的文本
     */
    public static void verbose(String msg, Map<String, String> replacements) {
        LOGGER.verbose(msg, replacements);
    }

    /**
     * 向后台发送一条 DEBUG 文本，仅当阈值不高于 {@link LogLevel#DEBUG} 时输出
     *
     * @param msg 发送的文本
     */
    public static void debug(String msg) {
        LOGGER.debug(msg);
    }

    /**
     * 向后台发送一条 DEBUG 文本，仅当阈值不高于 {@link LogLevel#DEBUG} 时输出
     *
     * @param msg        发送的文本
     * @param replacements 需要替换的文本
     */
    public static void debug(String msg, Map<String, String> replacements) {
        LOGGER.debug(msg, replacements);
    }

    /**
     * 向后台发送一条 DEBUG 文本并附带异常堆栈，仅当阈值不高于 {@link LogLevel#DEBUG} 时输出
     *
     * @param msg       发送的文本
     * @param throwable 需要打印堆栈的异常
     */
    public static void debug(String msg, Throwable throwable) {
        LOGGER.debug(msg, throwable);
    }

    /**
     * 向后台发送一条 WARN 文本，仅当阈值不高于 {@link LogLevel#WARN} 时输出
     *
     * @param msg 发送的文本
     */
    public static void warn(String msg) {
        LOGGER.warn(msg);
    }

    /**
     * 向后台发送一条 WARN 文本，仅当阈值不高于 {@link LogLevel#WARN} 时输出
     *
     * @param msg        发送的文本
     * @param replacements 需要替换的文本
     */
    public static void warn(String msg, Map<String, String> replacements) {
        LOGGER.warn(msg, replacements);
    }

    /**
     * 向后台发送一条 WARN 文本并附带异常堆栈，仅当阈值不高于 {@link LogLevel#WARN} 时输出
     *
     * @param msg       发送的文本
     * @param throwable 需要打印堆栈的异常
     */
    public static void warn(String msg, Throwable throwable) {
        LOGGER.warn(msg, throwable);
    }

    /**
     * 向后台发送一条 ERROR 文本，仅当阈值不高于 {@link LogLevel#ERROR} 时输出
     *
     * @param msg 发送的文本
     */
    public static void error(String msg) {
        LOGGER.error(msg);
    }

    /**
     * 向后台发送一条 ERROR 文本，仅当阈值不高于 {@link LogLevel#ERROR} 时输出
     *
     * @param msg        发送的文本
     * @param replacements 需要替换的文本
     */
    public static void error(String msg, Map<String, String> replacements) {
        LOGGER.error(msg, replacements);
    }

    /**
     * 向后台发送一条 ERROR 文本并附带异常堆栈
     *
     * @param msg       发送的文本
     * @param throwable 需要打印堆栈的异常
     */
    public static void error(String msg, Throwable throwable) {
        LOGGER.error(msg, throwable);
    }

}
