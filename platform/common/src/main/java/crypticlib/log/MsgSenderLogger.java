package crypticlib.log;

import crypticlib.chat.MsgSender;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

/**
 * 基于 {@link MsgSender} 的 {@link Logger} 默认实现。
 * <p>
 * 复用现有的着色输出通道：由本类拼接级别彩色标签，{@link MsgSender#log} 再拼接 [pluginName] 前缀，
 * 最终经各平台的 Invoker 写入控制台，输出形如 {@code [pluginName] [WARN] 消息}。
 */
public class MsgSenderLogger implements Logger {

    private final MsgSender msgSender;
    private volatile LogLevel level;

    /**
     * @param msgSender 惰性获取 MsgSender 的供应器(插件未初始化时可能为 null)
     * @param level             初始日志阈值
     */
    public MsgSenderLogger(@NotNull MsgSender msgSender, @NotNull LogLevel level) {
        this.msgSender = Objects.requireNonNull(msgSender);
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
    public void log(@NotNull LogLevel level, @NotNull String msg, @NotNull Map<String, String> replacements) {
        if (!isEnabled(level)) {
            return;
        }
        msgSender.log(level, msg, replacements);
    }

}
