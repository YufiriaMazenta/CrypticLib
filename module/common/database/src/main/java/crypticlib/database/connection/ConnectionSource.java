package crypticlib.database.connection;

import crypticlib.database.dialect.DatabaseDialect;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库连接源接口
 */
public interface ConnectionSource extends Closeable {

    /**
     * 获取数据库连接
     *
     * @return 数据库连接
     * @throws SQLException 获取连接失败时抛出
     */
    Connection getConnection() throws SQLException;

    /**
     * 释放连接（对于连接池实现，会将连接归还到池中）
     *
     * @param connection 要释放的连接
     * @throws SQLException 释放连接失败时抛出
     */
    void releaseConnection(Connection connection) throws SQLException;

    /**
     * 获取数据库方言
     *
     * @return 数据库方言
     */
    DatabaseDialect getDialect();

    /**
     * 关闭连接源，释放所有资源
     */
    @Override
    void close();

}
