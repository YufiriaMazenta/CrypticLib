package crypticlib.database.query;

import crypticlib.database.connection.ConnectionSource;
import crypticlib.database.dialect.DatabaseDialect;
import crypticlib.database.table.TableInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * UPDATE 语句构建器
 *
 * @param <T> 实体类型
 */
public class UpdateBuilder<T> {

    private final ConnectionSource connectionSource;
    private final TableInfo tableInfo;
    private final DatabaseDialect dialect;
    private final Where where;
    private final Map<String, Object> setValues = new LinkedHashMap<>();

    public UpdateBuilder(ConnectionSource connectionSource, TableInfo tableInfo) {
        this.connectionSource = connectionSource;
        this.tableInfo = tableInfo;
        this.dialect = connectionSource.getDialect();
        this.where = new Where(this, tableInfo, dialect);
    }

    /**
     * 设置要更新的列和值
     */
    public UpdateBuilder<T> set(String column, Object value) {
        setValues.put(column, value);
        return this;
    }

    /**
     * 获取 WHERE 条件构建器
     */
    public Where where() {
        return where;
    }

    /**
     * 执行更新
     *
     * @return 影响的行数
     */
    public int execute() throws SQLException {
        String sql = buildSql();
        Connection connection = connectionSource.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            List<Object> parameters = getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            return statement.executeUpdate();
        } finally {
            connectionSource.releaseConnection(connection);
        }
    }

    /**
     * 构建 UPDATE SQL
     */
    private String buildSql() {
        StringBuilder sqlBuilder = new StringBuilder("UPDATE ");
        sqlBuilder.append(dialect.quoteIdentifier(tableInfo.getTableName()));
        sqlBuilder.append(" SET ");

        StringJoiner setJoiner = new StringJoiner(", ");
        for (String column : setValues.keySet()) {
            setJoiner.add(dialect.quoteIdentifier(column) + " = ?");
        }
        sqlBuilder.append(setJoiner);

        sqlBuilder.append(where.buildSql());
        return sqlBuilder.toString();
    }

    /**
     * 获取所有参数（SET 值 + WHERE 条件值）
     */
    private List<Object> getParameters() {
        List<Object> parameters = new ArrayList<>(setValues.values());
        where.collectParameters(parameters);
        return parameters;
    }

}
