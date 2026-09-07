package crypticlib.database.transaction;

import crypticlib.database.connection.ConnectionSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 事务管理器
 */
public class TransactionManager {

    private TransactionManager() {
    }

    /**
     * 在事务中执行操作
     *
     * @param connectionSource 连接源
     * @param action           要执行的操作
     * @throws SQLException 操作失败时抛出
     */
    public static void withTransaction(ConnectionSource connectionSource, TransactionAction action) throws SQLException {
        Connection connection = connectionSource.getConnection();
        try {
            connection.setAutoCommit(false);
            action.execute();
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            throw new SQLException("事务执行失败", e);
        } finally {
            connection.setAutoCommit(true);
            connectionSource.releaseConnection(connection);
        }
    }

    /**
     * 在事务中执行操作并返回结果
     *
     * @param connectionSource 连接源
     * @param action           要执行的操作
     * @param <T>              返回类型
     * @return 操作结果
     * @throws SQLException 操作失败时抛出
     */
    public static <T> T withTransaction(ConnectionSource connectionSource, TransactionCallable<T> action) throws SQLException {
        Connection connection = connectionSource.getConnection();
        try {
            connection.setAutoCommit(false);
            T result = action.call();
            connection.commit();
            return result;
        } catch (Exception e) {
            connection.rollback();
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            throw new SQLException("事务执行失败", e);
        } finally {
            connection.setAutoCommit(true);
            connectionSource.releaseConnection(connection);
        }
    }

    /**
     * 事务操作接口
     */
    @FunctionalInterface
    public interface TransactionAction {
        void execute() throws Exception;
    }

    /**
     * 事务回调接口（带返回值）
     */
    @FunctionalInterface
    public interface TransactionCallable<T> {
        T call() throws Exception;
    }

}
