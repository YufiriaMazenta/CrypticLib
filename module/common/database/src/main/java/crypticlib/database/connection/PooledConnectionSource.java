package crypticlib.database.connection;

import crypticlib.database.dialect.DatabaseDialect;
import crypticlib.database.dialect.DialectManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final BlockingQueue<PooledConnection> freeConnections = new LinkedBlockingQueue<>();
    private final Map<Connection, PooledConnection> activeConnections = new ConcurrentHashMap<>();

    private int maxConnections = 10;
    private long maxIdleTimeMs = 600_000; // 10 分钟
    private long maxLifetimeMs = 1_800_000; // 30 分钟
    private long checkConnectionsEveryMs = 30_000; // 30 秒
    private boolean testBeforeGet = false;

    private ScheduledExecutorService heartbeatExecutor;
    private volatile boolean closed = false;
    private final AtomicInteger totalConnections = new AtomicInteger(0);

    public PooledConnectionSource(String url) {
        this(url, null, null);
    }

    public PooledConnectionSource(String url, DatabaseDialect dialect) {
        this(url, null, null, dialect);
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
        PooledConnection pooled = freeConnections.poll();
        while (pooled != null) {
            if (isPooledConnectionValid(pooled)) {
                pooled.updateLastUsedTime();
                activeConnections.put(pooled.getConnection(), pooled);
                return pooled.getConnection();
            }
            closeQuietly(pooled.getConnection());
            totalConnections.decrementAndGet();
            pooled = freeConnections.poll();
        }

        // 2. 创建新连接（不超过最大连接数）
        if (totalConnections.get() < maxConnections) {
            Connection connection = createNewConnection();
            pooled = new PooledConnection(connection);
            activeConnections.put(connection, pooled);
            totalConnections.incrementAndGet();
            return connection;
        }

        // 3. 等待空闲连接
        try {
            pooled = freeConnections.poll(10, TimeUnit.SECONDS);
            if (pooled == null) {
                throw new SQLException("获取连接超时，连接池已满且无空闲连接");
            }
            if (!isPooledConnectionValid(pooled)) {
                closeQuietly(pooled.getConnection());
                totalConnections.decrementAndGet();
                return getConnection(); // 重试
            }
            pooled.updateLastUsedTime();
            activeConnections.put(pooled.getConnection(), pooled);
            return pooled.getConnection();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("获取连接被中断", e);
        }
    }

    @Override
    public void releaseConnection(Connection connection) {
        if (connection == null) return;
        PooledConnection pooled = activeConnections.remove(connection);

        if (closed || !isConnectionValid(connection)) {
            closeQuietly(connection);
            totalConnections.decrementAndGet();
            return;
        }

        if (pooled != null) {
            pooled.updateLastUsedTime();
            if (!freeConnections.offer(pooled)) {
                closeQuietly(connection);
                totalConnections.decrementAndGet();
            }
        } else {
            closeQuietly(connection);
            totalConnections.decrementAndGet();
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
        for (PooledConnection pooled : activeConnections.values()) {
            closeQuietly(pooled.getConnection());
        }
        activeConnections.clear();
        // 关闭所有空闲连接
        PooledConnection pooled;
        while ((pooled = freeConnections.poll()) != null) {
            closeQuietly(pooled.getConnection());
        }
        totalConnections.set(0);
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

    private boolean isPooledConnectionValid(PooledConnection pooled) {
        if (!isConnectionValid(pooled.getConnection())) {
            return false;
        }
        long now = System.currentTimeMillis();
        // 检查最大存活时间
        if (now - pooled.getCreatedTime() > maxLifetimeMs) {
            return false;
        }
        return true;
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
        PooledConnection pooled;
        while ((pooled = freeConnections.peek()) != null) {
            boolean shouldEvict = false;
            // 检查连接是否无效
            if (!isConnectionValid(pooled.getConnection())) {
                shouldEvict = true;
            }
            // 检查空闲超时
            else if (maxIdleTimeMs > 0 && now - pooled.getLastUsedTime() > maxIdleTimeMs) {
                shouldEvict = true;
            }
            // 检查最大存活时间
            else if (maxLifetimeMs > 0 && now - pooled.getCreatedTime() > maxLifetimeMs) {
                shouldEvict = true;
            }

            if (shouldEvict) {
                freeConnections.poll();
                closeQuietly(pooled.getConnection());
                totalConnections.decrementAndGet();
            } else {
                break; // 队列是 FIFO，后面的更新，遇到有效的就停
            }
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

    public long getMaxIdleTimeMs() {
        return maxIdleTimeMs;
    }

    public long getMaxLifetimeMs() {
        return maxLifetimeMs;
    }

    public int getTotalConnections() {
        return totalConnections.get();
    }

    public int getFreeConnections() {
        return freeConnections.size();
    }

    public int getActiveConnections() {
        return activeConnections.size();
    }

    /**
     * 池化连接包装，记录创建时间和最后使用时间
     */
    private static class PooledConnection {
        private final Connection connection;
        private final long createdTime;
        private volatile long lastUsedTime;

        PooledConnection(Connection connection) {
            this.connection = connection;
            this.createdTime = System.currentTimeMillis();
            this.lastUsedTime = this.createdTime;
        }

        Connection getConnection() {
            return connection;
        }

        long getCreatedTime() {
            return createdTime;
        }

        long getLastUsedTime() {
            return lastUsedTime;
        }

        void updateLastUsedTime() {
            this.lastUsedTime = System.currentTimeMillis();
        }
    }

}
