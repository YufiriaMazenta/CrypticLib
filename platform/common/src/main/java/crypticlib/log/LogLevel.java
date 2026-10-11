package crypticlib.log;

/**
 * 日志级别。
 * <p>
 * severity 越大表示越严重；日志按“最低级别阈值”过滤：
 * 只有当消息级别不低于当前阈值时才会输出。{@link #OFF} 只作为阈值使用，可一键关闭全部输出。
 * 其余级别输出时会带上各自的彩色标签。
 */
public enum LogLevel {

    /**
     * 调试日志，默认不输出
     */
    DEBUG(0, "&7[DEBUG] "),
    /**
     * 详细日志(简单日志的详细版本)，默认不输出
     */
    VERBOSE(1, "&r[VERBOSE] "),
    /**
     * 简单日志，等价于旧的 {@code CrypticLib.info}，不额外添加级别标签
     */
    INFO(2, "&r[INFO] "),
    /**
     * 警告日志
     */
    WARN(3, "&e[WARN] "),
    /**
     * 错误日志
     */
    ERROR(4, "&c[ERROR] "),
    /**
     * 关闭全部日志输出，仅作为阈值使用
     */
    OFF(5, "");

    private final int severity;
    private final String tag;

    LogLevel(int severity, String tag) {
        this.severity = severity;
        this.tag = tag;
    }

    /**
     * @return 严重程度，越大越严重
     */
    public int severity() {
        return severity;
    }

    /**
     * @return 该级别在控制台输出时使用的彩色标签(含颜色代码)；INFO 与 OFF 为空串
     */
    public String tag() {
        return tag;
    }

    /**
     * 判断以当前级别作为阈值时，给定级别的消息是否应当输出
     *
     * @param message 消息自身的级别
     * @return 当 message 的 severity 不低于当前阈值时返回 true；阈值为 {@link #OFF} 时恒为 false
     */
    public boolean allows(LogLevel message) {
        return this != OFF && message.severity >= this.severity;
    }

    /**
     * 忽略大小写解析级别名，解析失败时返回默认值
     *
     * @param name       级别名，可为 null
     * @param defaultVal 解析失败时返回的默认级别
     * @return 解析得到的级别
     */
    public static LogLevel parse(String name, LogLevel defaultVal) {
        if (name == null) {
            return defaultVal;
        }
        String trimmed = name.trim();
        for (LogLevel level : values()) {
            if (level.name().equalsIgnoreCase(trimmed)) {
                return level;
            }
        }
        return defaultVal;
    }

}
