package crypticlib.log;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

/**
 * 日志器接口。
 * <p>
 * 提供 DEBUG / VERBOSE / INFO / WARN / ERROR 多级日志与级别过滤，级别语义见 {@link LogLevel}。
 * 实现类只需落地 {@link #log(LogLevel, String, Map)}，其余方法均基于它提供默认行为。
 */
public interface Logger {

    /**
     * @return 当前日志阈值
     */
    @NotNull
    LogLevel level();

    /**
     * 设置日志阈值
     *
     * @param level 新的阈值
     */
    void setLevel(@NotNull LogLevel level);

    /**
     * 判断给定级别的日志在当前阈值下是否会被输出
     *
     * @param level 待判断的级别
     * @return 是否输出
     */
    default boolean isEnabled(@NotNull LogLevel level) {
        return level().allows(level);
    }

    /**
     * 输出一条指定级别的日志，这是实现类唯一需要落地的方法
     *
     * @param level        日志级别
     * @param msg          日志文本
     * @param replacements 需要替换的文本
     */
    void log(@NotNull LogLevel level, @NotNull String msg, @NotNull Map<String, String> replacements);

    default void log(@NotNull LogLevel level, @NotNull String msg) {
        log(level, msg, Collections.<String, String>emptyMap());
    }

    default void verbose(@NotNull String msg) {
        log(LogLevel.VERBOSE, msg);
    }

    default void verbose(@NotNull String msg, @NotNull Map<String, String> replacements) {
        log(LogLevel.VERBOSE, msg, replacements);
    }

    default void info(@NotNull String msg) {
        log(LogLevel.INFO, msg);
    }

    default void info(@NotNull String msg, @NotNull Map<String, String> replacements) {
        log(LogLevel.INFO, msg, replacements);
    }

    default void warn(@NotNull String msg) {
        log(LogLevel.WARN, msg);
    }

    default void warn(@NotNull String msg, @NotNull Map<String, String> replacements) {
        log(LogLevel.WARN, msg, replacements);
    }

    /**
     * 输出一条 WARN 日志并附带异常堆栈
     *
     * @param msg       日志文本
     * @param throwable 需要打印堆栈的异常，可为 null
     */
    default void warn(@NotNull String msg, Throwable throwable) {
        warn(msg);
        if (throwable != null && isEnabled(LogLevel.WARN)) {
            throwable.printStackTrace();
        }
    }

    default void error(@NotNull String msg) {
        log(LogLevel.ERROR, msg);
    }

    default void error(@NotNull String msg, @NotNull Map<String, String> replacements) {
        log(LogLevel.ERROR, msg, replacements);
    }

    /**
     * 输出一条 ERROR 日志并附带异常堆栈
     *
     * @param msg       日志文本
     * @param throwable 需要打印堆栈的异常，可为 null
     */
    default void error(@NotNull String msg, Throwable throwable) {
        error(msg);
        if (throwable != null && isEnabled(LogLevel.ERROR)) {
            throwable.printStackTrace();
        }
    }

    default void debug(@NotNull String msg) {
        log(LogLevel.DEBUG, msg);
    }

    default void debug(@NotNull String msg, @NotNull Map<String, String> replacements) {
        log(LogLevel.DEBUG, msg, replacements);
    }

    /**
     * 输出一条 DEBUG 日志并附带异常堆栈，仅当阈值不高于 {@link LogLevel#DEBUG} 时输出
     *
     * @param msg       日志文本
     * @param throwable 需要打印堆栈的异常，可为 null
     */
    default void debug(@NotNull String msg, Throwable throwable) {
        debug(msg);
        if (throwable != null && isEnabled(LogLevel.DEBUG)) {
            throwable.printStackTrace();
        }
    }

}
