package crypticlib.log;

import crypticlib.CrypticLib;
import crypticlib.util.StringHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

/**
 * 基于平台原生 logger 的 {@link Logger} 抽象实现，子类只需实现 {@link #output(LogLevel, String, Throwable)}
 */
public abstract class NativeLogger implements Logger {

    private volatile LogLevel level;

    /**
     * @param level 初始日志阈值
     */
    protected NativeLogger(@NotNull LogLevel level) {
        this.level = Objects.requireNonNull(level);
    }

    @Override
    public @NotNull LogLevel level() {
        return level;
    }

    @Override
    public void setLevel(@NotNull LogLevel level) {
        this.level = Objects.requireNonNull(level);
    }

    @Override
    public void log(@NotNull LogLevel level, @NotNull String msg, @NotNull Map<String, String> replacements, Throwable throwable) {
        if (!isEnabled(level)) {
            return;
        }
        msg = StringHelper.replaceStrings(msg, replacements);
        String levelTag = level == LogLevel.DEBUG ? "[" + level.name() + "] " : "";
        String text = level.prefix() + "[" + CrypticLib.plugin().pluginName() + "] " + levelTag + msg;
        output(level, AnsiColorHelper.translate(text), throwable);
    }

    /**
     * 将日志输出到平台原生 logger
     */
    protected abstract void output(@NotNull LogLevel level, @NotNull String message, Throwable throwable);

}
