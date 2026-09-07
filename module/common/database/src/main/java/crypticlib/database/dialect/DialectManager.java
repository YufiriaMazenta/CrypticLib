package crypticlib.database.dialect;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 方言管理器，负责注册和识别数据库方言
 */
public class DialectManager {

    private static final Map<String, Supplier<DatabaseDialect>> REGISTRY = new LinkedHashMap<>();

    static {
        register("mysql", MysqlDialect::new);
        register("mariadb", MysqlDialect::new);
        register("postgresql", PostgresqlDialect::new);
        register("sqlite", SqliteDialect::new);
        register("h2", H2Dialect::new);
    }

    /**
     * 注册方言
     *
     * @param subprotocol JDBC subprotocol 名称
     * @param factory     方言工厂
     */
    public static void register(String subprotocol, Supplier<DatabaseDialect> factory) {
        REGISTRY.put(subprotocol, factory);
    }

    /**
     * 从 JDBC URL 自动识别方言
     *
     * @param url JDBC URL
     * @return 对应的方言实例
     */
    public static DatabaseDialect detectFromUrl(String url) {
        String subprotocol = extractSubprotocol(url);
        Supplier<DatabaseDialect> factory = REGISTRY.get(subprotocol);
        if (factory == null) {
            throw new UnsupportedOperationException("不支持的数据库类型: " + subprotocol);
        }
        return factory.get();
    }

    /**
     * 从 JDBC URL 提取 subprotocol
     *
     * @param url JDBC URL (格式: jdbc:<subprotocol>:<subname>)
     * @return subprotocol
     */
    public static String extractSubprotocol(String url) {
        if (url == null || !url.startsWith("jdbc:")) {
            throw new IllegalArgumentException("非法 JDBC URL: " + url);
        }
        int firstColon = 4; // "jdbc:".length() - 1
        int secondColon = url.indexOf(':', firstColon + 1);
        if (secondColon < 0) {
            throw new IllegalArgumentException("非法 JDBC URL: " + url);
        }
        return url.substring(firstColon + 1, secondColon);
    }

}
