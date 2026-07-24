package crypticlib.dependency;

import me.lucko.jarrelocator.Relocation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * JAR 包名重定位规则
 */
public class JarRelocation {

    private final String pattern;
    private final String relocatedPattern;

    public JarRelocation(@NotNull String pattern, @NotNull String relocatedPattern) {
        this.pattern = pattern.replace("#", "").replace("%", ".");
        this.relocatedPattern = relocatedPattern.replace("#", "").replace("%", ".");
    }

    @NotNull
    public String getPattern() {
        return pattern;
    }

    @NotNull
    public String getRelocatedPattern() {
        return relocatedPattern;
    }

    /**
     * 转换为 me.lucko jar-relocator 的 Relocation 对象
     */
    @NotNull
    public Relocation toRelocation() {
        return new Relocation(pattern, relocatedPattern);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JarRelocation)) return false;
        JarRelocation that = (JarRelocation) o;
        return Objects.equals(pattern, that.pattern) && Objects.equals(relocatedPattern, that.relocatedPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern, relocatedPattern);
    }
}
