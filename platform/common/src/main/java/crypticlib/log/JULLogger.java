package crypticlib.log;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 基于 {@link java.util.logging.Logger} 的 {@link NativeLogger} 实现，用于 Bukkit / Bungee
 */
public class JULLogger extends NativeLogger {

    private final Logger logger;

    /**
     * @param logger 平台原生 JUL 日志器
     * @param level  初始日志阈值
     */
    public JULLogger(@NotNull Logger logger, @NotNull LogLevel level) {
        super(level);
        this.logger = logger;
    }

    @Override
    protected void output(@NotNull LogLevel level, @NotNull String message, Throwable throwable) {
        logger.log(levelOf(level), message, throwable);
    }

    private static Level levelOf(LogLevel level) {
        switch (level) {
            case WARN:
                return Level.WARNING;
            case ERROR:
                return Level.SEVERE;
            default:
                return Level.INFO;
        }
    }

}
