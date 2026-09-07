package crypticlib.database.query;

import crypticlib.database.connection.ConnectionSource;
import crypticlib.database.dao.Dao;
import crypticlib.database.dialect.DatabaseDialect;
import crypticlib.database.table.TableInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 查询构建器
 *
 * @param <T> 实体类型
 */
public class QueryBuilder<T> {

    private final Dao<T> dao;
    private final ConnectionSource connectionSource;
    private final TableInfo tableInfo;
    private final Where<QueryBuilder<T>> where;
    private final List<OrderBy> orderByList = new ArrayList<>();
    private long limit = -1;
    private long offset = 0;

    public QueryBuilder(Dao<T> dao, ConnectionSource connectionSource, TableInfo tableInfo) {
        this.dao = dao;
        this.connectionSource = connectionSource;
        this.tableInfo = tableInfo;
        this.where = new Where<>(this, tableInfo, connectionSource.getDialect());
    }

    /**
     * 设置 WHERE 条件
     */
    public QueryBuilder<T> where(Consumer<Where<QueryBuilder<T>>> configurator) {
        configurator.accept(where);
        return this;
    }

    /**
     * 添加排序
     */
    public QueryBuilder<T> orderBy(String column, boolean ascending) {
        orderByList.add(new OrderBy(column, ascending));
        return this;
    }

    /**
     * 设置查询数量限制
     */
    public QueryBuilder<T> limit(long limit) {
        this.limit = limit;
        return this;
    }

    /**
     * 设置查询偏移量
     */
    public QueryBuilder<T> offset(long offset) {
        this.offset = offset;
        return this;
    }

    /**
     * 执行查询
     */
    public List<T> query() throws SQLException {
        return dao.query(this);
    }

    /**
     * 构建 SQL
     */
    public String buildSql() {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT * FROM ");
        sqlBuilder.append(connectionSource.getDialect().quoteIdentifier(tableInfo.getTableName()));

        // WHERE 子句
        sqlBuilder.append(where.buildSql());

        // ORDER BY 子句
        if (!orderByList.isEmpty()) {
            sqlBuilder.append(" ORDER BY ");
            for (int i = 0; i < orderByList.size(); i++) {
                if (i > 0) sqlBuilder.append(", ");
                OrderBy orderBy = orderByList.get(i);
                sqlBuilder.append(connectionSource.getDialect().quoteIdentifier(orderBy.column));
                sqlBuilder.append(orderBy.ascending ? " ASC" : " DESC");
            }
        }

        // LIMIT / OFFSET
        if (limit > 0) {
            sqlBuilder = new StringBuilder(connectionSource.getDialect().appendLimitOffset(sqlBuilder.toString(), limit, offset));
        }

        return sqlBuilder.toString();
    }

    /**
     * 获取查询参数列表
     */
    public List<Object> getParameters() {
        List<Object> parameters = new ArrayList<>();
        where.collectParameters(parameters);
        DatabaseDialect dialect = connectionSource.getDialect();
        for (int i = 0; i < parameters.size(); i++) {
            parameters.set(i, dialect.convertParameter(parameters.get(i)));
        }
        return parameters;
    }

    /**
     * 排序条件
     */
    private static class OrderBy {
        final String column;
        final boolean ascending;

        OrderBy(String column, boolean ascending) {
            this.column = column;
            this.ascending = ascending;
        }
    }

}
