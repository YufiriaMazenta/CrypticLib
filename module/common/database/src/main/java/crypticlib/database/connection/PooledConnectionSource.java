package crypticlib.database.connection;

import crypticlib.database.dialect.DatabaseDialect;
import crypticlib.database.dialect.DialectManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连接池数据库连接源（线程安全）
 * <p>
 * 适用于生产环境和多线程场景
 * <p>
 * 连接上限由信号量保证，并发借出时也不会超过 maxConnections。
 * 配置方法（setMaxConnections、setMaxIdleTimeMs、setMaxLifetimeMs、setCheckConnectionsEveryMs、setTestBeforeGet）
 * 需在首次获取连接前调用；其中 setMaxConnections 与 setCheckConnectionsEveryMs 在连接池开始使用后调用会抛出异常。
 * 心跳线程在首次获取连接时才会启动，未被使用过的连接源不会创建后台线程。
 */
public class PooledConnectionSource implements ConnectionSource {

    private static final long ACQUIRE_TIMEOUT_SECONDS = 10;

    private final String url;
    private final String user;
    private final String password;
    private final DatabaseDialect dialect;

    private final BlockingQueue<PooledConnection> freeConnections = new LinkedBlockingQueue<>();
    private final Map<Connection, PooledConnection> activeConnections = new ConcurrentHashMap<>();

    private volatile int maxConnections = 10;
    private volatile long maxIdleTimeMs = 600_000; // 10 分钟
    private volatile long maxLifetimeMs = 1_800_000; // 30 分钟
    private volatile long checkConnectionsEveryMs = 30_000; // 30 秒
    private volatile boolean testBeforeGet = false;

    private volatile Semaphore permits = new Semaphore(10);

    private volatile ScheduledExecutorService heartbeatExecutor;
    private final AtomicBoolean heartbeatStarted = new AtomicBoolean(false);
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
    }

    public PooledConnectionSource(String url, String user, String password, DatabaseDialect dialect) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.dialect = dialect;
    }

    @Override
    public Connection getConnection() throws SQLException {
        checkClosed();
        startHeartbeatIfNeeded();
        acquirePermit();

        boolean borrowed = false;
        try {
            PooledConnection pooled = pollValidConnection();
            if (pooled == null) {
                pooled = new PooledConnection(createNewConnection());
                totalConnections.incrementAndGet();
            }
            pooled.updateLastUsedTime();
            activeConnections.put(pooled.getConnection(), pooled);
            borrowed = true;
            return pooled.getConnection();
        } finally {
            // 获取失败时需要把许可还回去
            if (!borrowed) {
                permits.release();
            }
        }
    }

    @Override
    public void releaseConnection(Connection connection) {
        if (connection == null) return;

        PooledConnection pooled = activeConnections.remove(connection);
        if (pooled == null) {
            // 不是本连接池借出的连接（例如重复归还），直接忽略
            return;
        }

        try {
            if (closed || !isConnectionValid(connection)) {
                closeQuietly(connection);
                totalConnections.decrementAndGet();
                return;
            }
            pooled.updateLastUsedTime();
            if (!freeConnections.offer(pooled)) {
                closeQuietly(connection);
                totalConnections.decrementAndGet();
            }
        } finally {
            permits.release();
        }
    }

    @Override
    public DatabaseDialect getDialect() {
        return dialect;
    }

    @Override
    public boolean isOpen() {
        return !closed;
    }

    @Override
    public void close() {
        closed = true;
        ScheduledExecutorService executor = heartbeatExecutor;
        if (executor != null) {
            executor.shutdownNow();
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

    /**
     * 从空闲队列取出一个可用连接，无效的连接直接关闭
     */
    private PooledConnection pollValidConnection() {
        PooledConnection pooled = freeConnections.poll();
        while (pooled != null) {
            if (isPooledConnectionValid(pooled)) {
                return pooled;
            }
            closeQuietly(pooled.getConnection());
            totalConnections.decrementAndGet();
            pooled = freeConnections.poll();
        }
        return null;
    }

    private void acquirePermit() throws SQLException {
        try {
            if (!permits.tryAcquire(ACQUIRE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new SQLException("获取连接超时，连接池已满且无空闲连接");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("获取连接被中断", e);
        }
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
        // 检查最大存活时间
        return !isExpired(pooled, System.currentTimeMillis());
    }

    private boolean isExpired(PooledConnection pooled, long now) {
        if (maxIdleTimeMs > 0 && now - pooled.getLastUsedTime() > maxIdleTimeMs) {
            return true;
        }
        return maxLifetimeMs > 0 && now - pooled.getCreatedTime() > maxLifetimeMs;
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

    private void startHeartbeatIfNeeded() {
        if (checkConnectionsEveryMs <= 0) return;
        if (!heartbeatStarted.compareAndSet(false, true)) return;
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "database-heartbeat");
            t.setDaemon(true);
            return t;
        });
        heartbeatExecutor = executor;
        executor.scheduleAtFixedRate(this::checkConnections, checkConnectionsEveryMs, checkConnectionsEveryMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 清理空闲连接：逐个取出后判断，无效或超时的关闭，其余放回队列。
     * 不能使用 peek + poll 的两步操作，否则可能与其它线程的取用互相错位。
     */
    private void checkConnections() {
        if (closed) return;
        long now = System.currentTimeMillis();
        int freeCount = freeConnections.size();
        for (int i = 0; i < freeCount; i++) {
            PooledConnection pooled = freeConnections.poll();
            if (pooled == null) {
                return;
            }
            if (!isConnectionValid(pooled.getConnection()) || isExpired(pooled, now)) {
                closeQuietly(pooled.getConnection());
                totalConnections.decrementAndGet();
            } else {
                freeConnections.offer(pooled);
            }
        }
    }

    // 配置方法

    /**
     * 设置最大连接数，需在首次获取连接前调用
     */
    public PooledConnectionSource setMaxConnections(int maxConnections) {
        if (totalConnections.get() > 0) {
            throw new IllegalStateException("连接池已开始使用，无法修改最大连接数");
        }
        this.maxConnections = maxConnections;
        this.permits = new Semaphore(maxConnections);
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

    /**
     * 设置心跳检查间隔，需在首次获取连接前调用；小于等于 0 表示不启动心跳
     */
    public PooledConnectionSource setCheckConnectionsEveryMs(long checkConnectionsEveryMs) {
        if (heartbeatStarted.get()) {
            throw new IllegalStateException("心跳线程已启动，无法修改心跳检查间隔");
        }
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
