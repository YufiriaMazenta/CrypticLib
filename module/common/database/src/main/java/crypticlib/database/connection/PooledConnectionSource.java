package crypticlib.database.connection;

import crypticlib.database.dialect.DatabaseDialect;
import crypticlib.database.dialect.DialectManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
            //返回代理: 让按 AutoCloseable 习惯调用 close() 的调用方也能正确归还,
            //否则每 close 一次就会永久泄漏一个连接许可, 累积到上限后所有借用都会超时
            return PooledConnectionHandle.wrap(pooled.getConnection(), this);
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

        Connection rawConnection = PooledConnectionHandle.unwrap(connection);
        PooledConnection pooled = activeConnections.remove(rawConnection);
        if (pooled == null) {
            // 不是本连接池借出的连接（例如重复归还），直接忽略
            return;
        }

        try {
            //归还前必须把事务状态复位: autoCommit=false 或带着未提交事务的连接一旦回到池里,
            //下一个借用者的写入会静默不提交, 上一个借用者的行锁也会一直持有
            if (!resetTransactionState(rawConnection)
                || closed
                || !isConnectionValid(rawConnection)) {
                closeQuietly(rawConnection);
                totalConnections.decrementAndGet();
                return;
            }
            pooled.updateLastUsedTime();
            if (!freeConnections.offer(pooled)) {
                closeQuietly(rawConnection);
                totalConnections.decrementAndGet();
            }
        } finally {
            permits.release();
        }
    }

    /**
     * 把连接的事务状态复位: 回滚未提交的事务并把 autoCommit 改回 true
     *
     * @return 是否复位成功; 失败时调用方应关闭该连接而不是归还到池中
     */
    private boolean resetTransactionState(Connection connection) {
        try {
            if (connection.getAutoCommit()) {
                return true;
            }
            connection.rollback();
            connection.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            return false;
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
                throw new SQLException("Timed out while acquiring a connection: the pool is full and no idle connection is available");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while acquiring a connection", e);
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
            throw new IllegalStateException("PooledConnectionSource has been closed");
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
        if (maxConnections < 1) {
            throw new IllegalArgumentException("maxConnections must be at least 1, but got " + maxConnections);
        }
        if (totalConnections.get() > 0) {
            throw new IllegalStateException("Cannot change max connections after the pool has been used");
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
            throw new IllegalStateException("Cannot change the heartbeat interval after the heartbeat thread has started");
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
     * 连接代理: 把 close() 转成归还到连接池。
     * <p>
     * 池对外只提供 {@link #releaseConnection(Connection)}, 但返回的 Connection 本身是 AutoCloseable:
     * 调用方按 try-with-resources 使用时, 若 close() 不归还, 就会永久漏掉一个许可。
     * 这里拦截 close(), 把它转成正常的归还流程。
     */
    private static final class PooledConnectionHandle implements InvocationHandler {

        private final Connection delegate;
        private final PooledConnectionSource source;
        private final AtomicBoolean released = new AtomicBoolean(false);

        private PooledConnectionHandle(Connection delegate, PooledConnectionSource source) {
            this.delegate = delegate;
            this.source = source;
        }

        static Connection wrap(Connection delegate, PooledConnectionSource source) {
            return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new PooledConnectionHandle(delegate, source)
            );
        }

        /**
         * 取出代理背后的真实连接; 非本类代理原样返回
         */
        static Connection unwrap(Connection connection) {
            if (connection == null) {
                return null;
            }
            if (!Proxy.isProxyClass(connection.getClass())) {
                return connection;
            }
            InvocationHandler handler = Proxy.getInvocationHandler(connection);
            if (handler instanceof PooledConnectionHandle) {
                return ((PooledConnectionHandle) handler).delegate;
            }
            return connection;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if ("close".equals(methodName) && (args == null || args.length == 0)) {
                release();
                return null;
            }
            if ("isClosed".equals(methodName) && (args == null || args.length == 0)) {
                return released.get() || delegate.isClosed();
            }
            if ("equals".equals(methodName) && args != null && args.length == 1) {
                Object other = args[0];
                if (proxy == other) {
                    return true;
                }
                if (!(other instanceof Connection)) {
                    return false;
                }
                return delegate.equals(unwrap((Connection) other));
            }
            if ("hashCode".equals(methodName) && (args == null || args.length == 0)) {
                return delegate.hashCode();
            }
            if ("toString".equals(methodName) && (args == null || args.length == 0)) {
                return "PooledConnection[" + delegate + "]";
            }
            try {
                return method.invoke(delegate, args);
            } catch (InvocationTargetException e) {
                //把驱动抛出的原始异常透出, 不要包成 InvocationTargetException
                throw e.getCause();
            }
        }

        private void release() {
            if (released.compareAndSet(false, true)) {
                source.releaseConnection(delegate);
            }
        }
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
