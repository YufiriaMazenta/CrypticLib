package crypticlib.database.connection;

import crypticlib.database.dialect.DatabaseDialect;
import crypticlib.database.dialect.DialectManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 普通数据库连接源（单连接复用，非线程安全）
 * <p>
 * 适用于开发、测试或单线程场景
 */
public class JdbcConnectionSource implements ConnectionSource {

    private final String url;
    private final String user;
    private final String password;
    private final DatabaseDialect dialect;
    private Connection connection;
    private boolean closed = false;

    public JdbcConnectionSource(String url) {
        this(url, null, null);
    }

    public JdbcConnectionSource(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.dialect = DialectManager.detectFromUrl(url);
    }

    public JdbcConnectionSource(String url, String user, String password, DatabaseDialect dialect) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.dialect = dialect;
    }

    @Override
    public Connection getConnection() throws SQLException {
        checkClosed();
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }

    @Override
    public void releaseConnection(Connection connection) {
        // 非连接池实现，不关闭连接，保持复用
    }

    @Override
    public DatabaseDialect getDialect() {
        return dialect;
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
        closed = true;
    }

    private void checkClosed() {
        if (closed) {
            throw new IllegalStateException("ConnectionSource 已关闭");
        }
    }

}
