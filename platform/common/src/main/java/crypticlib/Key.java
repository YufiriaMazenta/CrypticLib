package crypticlib;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Minecraft的资源标识符,包含命名空间和key两个字段
 * <p>
 * 格式为 "namespace:key",例如 "minecraft:stone"
 */
public final class Key implements Comparable<Key> {

    /**
     * 默认命名空间
     */
    public static final String DEFAULT_NAMESPACE = "minecraft";

    private final String namespace;
    private final String key;

    private Key(@NotNull String namespace, @NotNull String key) {
        this.namespace = namespace;
        this.key = key;
    }

    /**
     * 从字符串解析Key,格式为 "namespace:key"
     * <p>
     * 如果没有命名空间前缀,则使用默认命名空间 "minecraft"
     * <p>
     * 如果格式不合法,返回null
     *
     * @param string 格式为 "namespace:key" 的字符串
     * @return Key实例,如果格式不合法则返回null
     */
    public static @Nullable Key key(@NotNull String string) {
        int index = string.indexOf(':');
        if (index == -1) {
            return key(DEFAULT_NAMESPACE, string);
        }
        return key(string.substring(0, index), string.substring(index + 1));
    }

    /**
     * 创建Key实例
     * <p>
     * 如果格式不合法,返回null
     *
     * @param namespace 命名空间
     * @param key 键
     * @return Key实例,如果格式不合法则返回null
     */
    public static @Nullable Key key(@NotNull String namespace, @NotNull String key) {
        if (!isValidNamespace(namespace)) {
            return null;
        }
        if (!isValidKey(key)) {
            return null;
        }
        return new Key(namespace, key);
    }

    /**
     * 验证命名空间是否合法
     * <p>
     * 合法字符: [a-z0-9_.-]
     *
     * @param namespace 命名空间
     * @return 是否合法
     */
    public static boolean isValidNamespace(@Nullable String namespace) {
        if (namespace == null || namespace.isEmpty()) {
            return false;
        }
        for (int i = 0, length = namespace.length(); i < length; i++) {
            if (!isValidNamespaceChar(namespace.charAt(i))) {
                return false;
            }
        }
        return !namespace.equalsIgnoreCase("..");
    }

    /**
     * 验证key是否合法
     * <p>
     * 合法字符: [a-z0-9._/-]
     *
     * @param key 键
     * @return 是否合法
     */
    public static boolean isValidKey(@Nullable String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        for (int i = 0, length = key.length(); i < length; i++) {
            if (!isValidKeyChar(key.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 验证命名空间字符是否合法
     *
     * @param c 字符
     * @return 是否合法
     */
    private static boolean isValidNamespaceChar(char c) {
        return (c >= 'a' && c <= 'z')
            || (c >= '0' && c <= '9')
            || c == '.'
            || c == '_'
            || c == '-';
    }

    /**
     * 验证key字符是否合法
     *
     * @param c 字符
     * @return 是否合法
     */
    private static boolean isValidKeyChar(char c) {
        return (c >= 'a' && c <= 'z')
            || (c >= '0' && c <= '9')
            || c == '.'
            || c == '_'
            || c == '-'
            || c == '/';
    }

    /**
     * 获取命名空间
     *
     * @return 命名空间
     */
    public @NotNull String namespace() {
        return namespace;
    }

    /**
     * 获取键
     *
     * @return 键
     */
    public @NotNull String key() {
        return key;
    }

    @Override
    public int compareTo(@NotNull Key other) {
        int namespaceCompare = this.namespace.compareTo(other.namespace);
        if (namespaceCompare != 0) {
            return namespaceCompare;
        }
        return this.key.compareTo(other.key);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Key)) return false;
        Key other = (Key) obj;
        return this.namespace.equals(other.namespace) && this.key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, key);
    }

    @Override
    public @NotNull String toString() {
        return namespace + ":" + key;
    }

}
