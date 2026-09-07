package crypticlib.database.query;

import crypticlib.database.connection.ConnectionSource;
import crypticlib.database.dialect.DatabaseDialect;
import crypticlib.database.table.TableInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * DELETE 语句构建器
 *
 * @param <T> 实体类型
 */
public class DeleteBuilder<T> {

    private final ConnectionSource connectionSource;
    private final TableInfo tableInfo;
    private final DatabaseDialect dialect;
    private final Where<DeleteBuilder<T>> where;

    public DeleteBuilder(ConnectionSource connectionSource, TableInfo tableInfo) {
        this.connectionSource = connectionSource;
        this.tableInfo = tableInfo;
        this.dialect = connectionSource.getDialect();
        this.where = new Where<>(this, tableInfo, dialect);
    }

    /**
     * 设置 WHERE 条件
     */
    public DeleteBuilder<T> where(Consumer<Where<DeleteBuilder<T>>> configurator) {
        configurator.accept(where);
        return this;
    }

    /**
     * 执行删除
     *
     * @return 影响的行数
     */
    public int execute() throws SQLException {
        String sql = buildSql();
        Connection connection = connectionSource.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            List<Object> parameters = new ArrayList<>();
            where.collectParameters(parameters);
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            return statement.executeUpdate();
        } finally {
            connectionSource.releaseConnection(connection);
        }
    }

    /**
     * 构建 DELETE SQL
     */
    private String buildSql() {
        StringBuilder sqlBuilder = new StringBuilder("DELETE FROM ");
        sqlBuilder.append(dialect.quoteIdentifier(tableInfo.getTableName()));
        sqlBuilder.append(where.buildSql());
        return sqlBuilder.toString();
    }

}
