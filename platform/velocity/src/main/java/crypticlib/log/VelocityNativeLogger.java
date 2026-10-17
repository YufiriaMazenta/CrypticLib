package crypticlib.log;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * 基于 SLF4J {@link Logger} 的 {@link NativeLogger} 实现，用于 Velocity
 */
public class VelocityNativeLogger extends NativeLogger {

    private final Logger logger;

    /**
     * @param logger 平台原生 SLF4J 日志器
     * @param level  初始日志阈值
     */
    public VelocityNativeLogger(@NotNull Logger logger, @NotNull LogLevel level) {
        super(level);
        this.logger = logger;
    }

    @Override
    protected void output(@NotNull LogLevel level, @NotNull String message, Throwable throwable) {
        switch (level) {
            case WARN:
                logger.warn(message, throwable);
                break;
            case ERROR:
                logger.error(message, throwable);
                break;
            default:
                logger.info(message, throwable);
                break;
        }
    }

}
