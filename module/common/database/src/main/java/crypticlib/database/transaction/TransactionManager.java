package crypticlib.database.transaction;

import crypticlib.database.connection.ConnectionSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 事务管理器
 * <p>
 * 事务中的操作必须使用回调传入的 Connection（DAO 的同名带 Connection 重载）执行，
 * 否则操作会另借一条连接、脱离事务，回滚将不会生效。
 * <p>
 * 不支持嵌套事务：在事务回调内再次调用 withTransaction 会直接抛出 IllegalStateException，
 * 避免内层提交导致外层事务被提前提交。
 */
public class TransactionManager {

    private static final ThreadLocal<Boolean> IN_TRANSACTION = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private TransactionManager() {
    }

    /**
     * 在事务中执行操作
     *
     * @param connectionSource 连接源
     * @param action           要执行的操作，使用其参数中的 Connection 执行数据库操作
     * @throws SQLException 操作失败时抛出
     */
    public static void withTransaction(ConnectionSource connectionSource, TransactionAction action) throws SQLException {
        executeTransaction(connectionSource, connection -> {
            action.execute(connection);
            return null;
        });
    }

    /**
     * 在事务中执行操作并返回结果
     * <p>
     * 因 lambda 在「带返回值」与「无返回值」两个回调接口之间会产生重载歧义，
     * 这里使用独立方法名而不是 withTransaction 的重载。
     *
     * @param connectionSource 连接源
     * @param action           要执行的操作，使用其参数中的 Connection 执行数据库操作
     * @param <T>              返回类型
     * @return 操作结果
     * @throws SQLException 操作失败时抛出
     */
    public static <T> T withTransactionResult(ConnectionSource connectionSource, TransactionCallable<T> action) throws SQLException {
        return executeTransaction(connectionSource, action);
    }

    private static <T> T executeTransaction(ConnectionSource connectionSource, TransactionCallable<T> action) throws SQLException {
        if (IN_TRANSACTION.get()) {
            throw new IllegalStateException("Nested transactions are not supported, complete all operations in the same transaction callback");
        }
        IN_TRANSACTION.set(Boolean.TRUE);
        try {
            Connection connection = connectionSource.getConnection();
            try {
                connection.setAutoCommit(false);
                T result = action.call(connection);
                connection.commit();
                return result;
            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    // 回滚失败不能覆盖原始异常
                    e.addSuppressed(rollbackError);
                }
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                throw new SQLException("Transaction execution failed", e);
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // 归还连接前恢复自动提交失败，忽略，避免覆盖真正的失败原因
                }
                connectionSource.releaseConnection(connection);
            }
        } finally {
            IN_TRANSACTION.remove();
        }
    }

    /**
     * 事务操作接口
     */
    @FunctionalInterface
    public interface TransactionAction {
        void execute(Connection connection) throws Exception;
    }

    /**
     * 事务回调接口（带返回值）
     */
    @FunctionalInterface
    public interface TransactionCallable<T> {
        T call(Connection connection) throws Exception;
    }

}
