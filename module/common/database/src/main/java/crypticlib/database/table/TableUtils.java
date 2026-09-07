package crypticlib.database.table;

import crypticlib.database.connection.ConnectionSource;
import crypticlib.database.dialect.DatabaseDialect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 表操作工具类
 */
public class TableUtils {

    private TableUtils() {
    }

    /**
     * 创建表（如果不存在）
     */
    public static void createTableIfNotExists(ConnectionSource source, Class<?> entityClass) throws SQLException {
        TableInfo tableInfo = TableInfo.of(entityClass);
        DatabaseDialect dialect = source.getDialect();
        String sql = dialect.generateCreateTableSql(tableInfo);

        Connection connection = source.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        } finally {
            source.releaseConnection(connection);
        }
    }

    /**
     * 创建表
     */
    public static void createTable(ConnectionSource source, Class<?> entityClass) throws SQLException {
        TableInfo tableInfo = TableInfo.of(entityClass);
        DatabaseDialect dialect = source.getDialect();
        String sql = dialect.generateCreateTableSql(tableInfo);

        // 移除 IF NOT EXISTS
        sql = sql.replace("IF NOT EXISTS ", "");

        Connection connection = source.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        } finally {
            source.releaseConnection(connection);
        }
    }

    /**
     * 删除表
     */
    public static void dropTable(ConnectionSource source, Class<?> entityClass) throws SQLException {
        TableInfo tableInfo = TableInfo.of(entityClass);
        DatabaseDialect dialect = source.getDialect();
        String sql = "DROP TABLE IF EXISTS " + dialect.quoteIdentifier(tableInfo.getTableName());

        Connection connection = source.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        } finally {
            source.releaseConnection(connection);
        }
    }

    /**
     * 清空表
     */
    public static void clearTable(ConnectionSource source, Class<?> entityClass) throws SQLException {
        TableInfo tableInfo = TableInfo.of(entityClass);
        String sql = "DELETE FROM " + source.getDialect().quoteIdentifier(tableInfo.getTableName());

        Connection connection = source.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        } finally {
            source.releaseConnection(connection);
        }
    }

}
