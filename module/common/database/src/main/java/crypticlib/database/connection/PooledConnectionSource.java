package crypticlib.database.connection;

import crypticlib.database.dialect.DatabaseDialect;
import crypticlib.database.dialect.DialectManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 连接池数据库连接源（线程安全）
 * <p>
 * 适用于生产环境和多线程场景
 */
public class PooledConnectionSource implements ConnectionSource {

    private final String url;
    private final String user;
    private final String password;
    private final DatabaseDialect dialect;

    private final BlockingQueue<Connection> freeConnections = new LinkedBlockingQueue<>();
    private final Map<Connection, Long> activeConnections = new ConcurrentHashMap<>();

    private int maxConnections = 10;
    private long maxIdleTimeMs = 600_000; // 10 分钟
    private long maxLifetimeMs = 1_800_000; // 30 分钟
    private long checkConnectionsEveryMs = 30_000; // 30 秒
    private boolean testBeforeGet = false;

    private ScheduledExecutorService heartbeatExecutor;
    private volatile boolean closed = false;
    private volatile int totalConnections = 0;

    public PooledConnectionSource(String url) {
        this(url, null, null);
    }

    public PooledConnectionSource(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.dialect = DialectManager.detectFromUrl(url);
        startHeartbeat();
    }

    public PooledConnectionSource(String url, String user, String password, DatabaseDialect dialect) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.dialect = dialect;
        startHeartbeat();
    }

    @Override
    public Connection getConnection() throws SQLException {
        checkClosed();

        // 1. 尝试从空闲队列获取
        Connection connection = freeConnections.poll();
        while (connection != null) {
            if (isConnectionValid(connection)) {
                activeConnections.put(connection, System.currentTimeMillis());
                return connection;
            }
            closeQuietly(connection);
            totalConnections--;
            connection = freeConnections.poll();
        }

        // 2. 创建新连接（不超过最大连接数）
        if (totalConnections < maxConnections) {
            connection = createNewConnection();
            activeConnections.put(connection, System.currentTimeMillis());
            totalConnections++;
            return connection;
        }

        // 3. 等待空闲连接
        try {
            connection = freeConnections.poll(10, TimeUnit.SECONDS);
            if (connection == null) {
                throw new SQLException("获取连接超时，连接池已满且无空闲连接");
            }
            if (!isConnectionValid(connection)) {
                closeQuietly(connection);
                totalConnections--;
                return getConnection(); // 重试
            }
            activeConnections.put(connection, System.currentTimeMillis());
            return connection;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("获取连接被中断", e);
        }
    }

    @Override
    public void releaseConnection(Connection connection) {
        if (connection == null) return;
        activeConnections.remove(connection);

        if (closed || !isConnectionValid(connection)) {
            closeQuietly(connection);
            totalConnections--;
            return;
        }

        if (!freeConnections.offer(connection)) {
            closeQuietly(connection);
            totalConnections--;
        }
    }

    @Override
    public DatabaseDialect getDialect() {
        return dialect;
    }

    @Override
    public void close() {
        closed = true;
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdownNow();
        }
        // 关闭所有活跃连接
        for (Connection conn : activeConnections.keySet()) {
            closeQuietly(conn);
        }
        activeConnections.clear();
        // 关闭所有空闲连接
        Connection conn;
        while ((conn = freeConnections.poll()) != null) {
            closeQuietly(conn);
        }
        totalConnections = 0;
    }

    private Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private boolean isConnectionValid(Connection connection) {
        try {
            if (testBeforeGet) {
                return connection.isValid(5);
            }
            return !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private void closeQuietly(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }

    private void checkClosed() {
        if (closed) {
            throw new IllegalStateException("PooledConnectionSource 已关闭");
        }
    }

    private void startHeartbeat() {
        if (checkConnectionsEveryMs <= 0) return;
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "database-heartbeat");
            t.setDaemon(true);
            return t;
        });
        heartbeatExecutor.scheduleAtFixedRate(this::checkConnections, checkConnectionsEveryMs, checkConnectionsEveryMs, TimeUnit.MILLISECONDS);
    }

    private void checkConnections() {
        if (closed) return;
        long now = System.currentTimeMillis();
        Connection conn;
        while ((conn = freeConnections.peek()) != null) {
            if (!isConnectionValid(conn)) {
                freeConnections.poll();
                closeQuietly(conn);
                totalConnections--;
                continue;
            }
            break;
        }
    }

    // 配置方法

    public PooledConnectionSource setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public PooledConnectionSource setMaxIdleTimeMs(long maxIdleTimeMs) {
        this.maxIdleTimeMs = maxIdleTimeMs;
        return this;
    }

    public PooledConnectionSource setMaxLifetimeMs(long maxLifetimeMs) {
        this.maxLifetimeMs = maxLifetimeMs;
        return this;
    }

    public PooledConnectionSource setCheckConnectionsEveryMs(long checkConnectionsEveryMs) {
        this.checkConnectionsEveryMs = checkConnectionsEveryMs;
        return this;
    }

    public PooledConnectionSource setTestBeforeGet(boolean testBeforeGet) {
        this.testBeforeGet = testBeforeGet;
        return this;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public int getTotalConnections() {
        return totalConnections;
    }

    public int getFreeConnections() {
        return freeConnections.size();
    }

    public int getActiveConnections() {
        return activeConnections.size();
    }

}
