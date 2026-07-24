package crypticlib.libloader;

import org.jetbrains.annotations.NotNull;

/**
 * 表示一个包名重定位规则
 */
public class Relocation {

    private final @NotNull String from;
    private final @NotNull String to;

    public Relocation(@NotNull String from, @NotNull String to) {
        // 支持用 % 代替 . 作为包名分隔符，避免 Shadow relocate 误伤
        this.from = from.replace('%', '.');
        this.to = to.replace('%', '.');
    }

    /**
     * 创建一个 Relocation 实例
     *
     * @param from 原始包名
     * @param to   目标包名
     * @return Relocation 实例
     */
    public static Relocation of(@NotNull String from, @NotNull String to) {
        return new Relocation(from, to);
    }

    @NotNull
    public String from() {
        return from;
    }

    @NotNull
    public String to() {
        return to;
    }

}
