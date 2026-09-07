package crypticlib.database.statement;

import crypticlib.database.connection.ConnectionSource;
import crypticlib.database.dao.Dao;
import crypticlib.database.dialect.DatabaseDialect;
import crypticlib.database.table.TableInfo;

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

    private final Dao<T> dao;
    private final ConnectionSource connectionSource;
    private final TableInfo tableInfo;
    private final DatabaseDialect dialect;
    private final Where<DeleteBuilder<T>> where;

    public DeleteBuilder(Dao<T> dao, ConnectionSource connectionSource, TableInfo tableInfo) {
        this.dao = dao;
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
    public int delete() throws SQLException {
        return dao.delete(this);
    }

    /**
     * 构建 DELETE SQL
     */
    public String buildSql() {
        StringBuilder sqlBuilder = new StringBuilder("DELETE FROM ");
        sqlBuilder.append(dialect.quoteIdentifier(tableInfo.getTableName()));
        sqlBuilder.append(where.buildSql());
        return sqlBuilder.toString();
    }

    /**
     * 获取参数列表
     */
    public List<Object> getParameters() {
        List<Object> parameters = new ArrayList<>();
        where.collectParameters(parameters);
        for (int i = 0; i < parameters.size(); i++) {
            parameters.set(i, dialect.convertParameter(parameters.get(i)));
        }
        return parameters;
    }

}
