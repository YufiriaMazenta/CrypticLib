package crypticlib.database;

import crypticlib.database.connection.ConnectionSource;
import crypticlib.database.connection.JdbcConnectionSource;
import crypticlib.database.connection.PooledConnectionSource;
import crypticlib.database.table.TableUtils;
import org.junit.jupiter.api.Assumptions;

import java.io.File;
import java.sql.SQLException;
import java.util.Locale;

/**
 * 测试后端的统一切换入口
 * <p>
 * 由系统属性 {@code crypticlib.test.database} 选择后端（Gradle 里对应 test / testSqlite / testMysql 任务）：
 * <ul>
 *     <li>{@code h2}（默认）：内存库，随 JVM 消失，最快；</li>
 *     <li>{@code sqlite}：build/sqlite-test 下的文件库，连接池的多条连接指向同一个库文件，
 *         跨连接可见性用例才有意义（内存库每条连接都是独立的库）；</li>
 *     <li>{@code mysql}：本机服务，所有用例复用同一个测试库，用 {@link #resetTable} 保证可重复执行。</li>
 * </ul>
 * 连接串与账号可以通过系统属性改写，便于在别的机器上跑：{@code crypticlib.test.mysql.url}、
 * {@code crypticlib.test.mysql.user}、{@code crypticlib.test.mysql.password}。
 */
final class TestDatabases {

    private static final String BACKEND_PROPERTY = "crypticlib.test.database";

    private static final String MYSQL_URL_PROPERTY = "crypticlib.test.mysql.url";
    private static final String MYSQL_USER_PROPERTY = "crypticlib.test.mysql.user";
    private static final String MYSQL_PASSWORD_PROPERTY = "crypticlib.test.mysql.password";

    private static final String DEFAULT_MYSQL_URL = "jdbc:mysql://127.0.0.1:3306/crypticlib_test"
        + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true"
        + "&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8";

    /**
     * SQLite 库文件目录，放在模块的 build 下，build 清理时一并消失
     */
    private static final File SQLITE_DIR = new File("build/sqlite-test");

    private TestDatabases() {
    }

    static String backend() {
        return System.getProperty(BACKEND_PROPERTY, "h2").toLowerCase(Locale.ROOT);
    }

    static boolean isSqlite() {
        return "sqlite".equals(backend());
    }

    static boolean isMysql() {
        return "mysql".equals(backend());
    }

    /**
     * 当前后端的 JDBC URL
     *
     * @param name 数据库名，H2/SQLite 用它区分不同的库，MySQL 复用同一个测试库
     */
    static String url(String name) {
        switch (backend()) {
            case "sqlite":
                if (!SQLITE_DIR.exists() && !SQLITE_DIR.mkdirs()) {
                    throw new IllegalStateException("Cannot create SQLite test directory: " + SQLITE_DIR.getAbsolutePath());
                }
                return "jdbc:sqlite:" + new File(SQLITE_DIR, name + ".db").getAbsolutePath();
            case "mysql":
                return System.getProperty(MYSQL_URL_PROPERTY, DEFAULT_MYSQL_URL);
            default:
                return "jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1";
        }
    }

    /**
     * 按当前后端创建连接源；方言仍由 DialectManager 从 URL 自动识别
     */
    static JdbcConnectionSource source(String name) {
        if (isMysql()) {
            return new JdbcConnectionSource(url(name), mysqlUser(), mysqlPassword());
        }
        return new JdbcConnectionSource(url(name));
    }

    /**
     * 按当前后端创建连接池连接源
     * <p>
     * 连接池只接收 URL，MySQL 的账号密码必须在这里一并传入，否则驱动会退回当前系统账号
     */
    static PooledConnectionSource pooledSource(String name) {
        if (isMysql()) {
            return new PooledConnectionSource(url(name), mysqlUser(), mysqlPassword());
        }
        return new PooledConnectionSource(url(name));
    }

    private static String mysqlUser() {
        return System.getProperty(MYSQL_USER_PROPERTY, "root");
    }

    private static String mysqlPassword() {
        return System.getProperty(MYSQL_PASSWORD_PROPERTY, "123456");
    }

    /**
     * 重建实体对应的表
     * <p>
     * 每个用例都从空表开始：H2 内存库随 JVM 消失、SQLite 库文件可能残留上次的表结构，
     * MySQL 的表是跨运行持久存在的，只有先删后建才能保证固定主键的用例可重复执行
     */
    static void resetTable(ConnectionSource source, Class<?> entityClass) throws SQLException {
        TableUtils.dropTable(source, entityClass);
        TableUtils.createTableIfNotExists(source, entityClass);
    }

    /**
     * 查询后端版本号的 SQL，用于在测试日志里记录本次验证的后端环境
     */
    static String versionSql() {
        switch (backend()) {
            case "sqlite":
                return "SELECT sqlite_version()";
            case "mysql":
                return "SELECT VERSION()";
            default:
                return "SELECT H2VERSION()";
        }
    }

    /**
     * 后端不可用时跳过用例而不是失败：没有本地数据库的机器也能跑通构建
     */
    static void assumeAvailable() {
        if (!isMysql()) {
            return;
        }
        ConnectionSource probe = null;
        try {
            probe = source("probe");
            probe.getConnection();
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "MySQL is not reachable at " + url("probe") + ": " + e.getMessage());
        } finally {
            if (probe != null) {
                probe.close();
            }
        }
    }

}