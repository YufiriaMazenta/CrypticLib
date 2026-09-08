package crypticlib.database.statement;

import crypticlib.database.connection.ConnectionSource;
import crypticlib.database.dao.Dao;
import crypticlib.database.dialect.DatabaseDialect;
import crypticlib.database.table.TableInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Consumer;

/**
 * UPDATE 语句构建器
 *
 * @param <T> 实体类型
 */
public class UpdateBuilder<T> {

    private final Dao<T> dao;
    private final ConnectionSource connectionSource;
    private final TableInfo tableInfo;
    private final DatabaseDialect dialect;
    private final Where<UpdateBuilder<T>> where;
    private final Map<String, Object> setValues = new LinkedHashMap<>();

    public UpdateBuilder(Dao<T> dao, ConnectionSource connectionSource, TableInfo tableInfo) {
        this.dao = dao;
        this.connectionSource = connectionSource;
        this.tableInfo = tableInfo;
        this.dialect = connectionSource.getDialect();
        this.where = new Where<>(this, tableInfo, dialect);
    }

    /**
     * 设置要更新的列和值
     */
    public UpdateBuilder<T> set(String column, Object value) {
        setValues.put(column, value);
        return this;
    }

    /**
     * 设置 WHERE 条件
     */
    public UpdateBuilder<T> where(Consumer<Where<UpdateBuilder<T>>> configurator) {
        configurator.accept(where);
        return this;
    }

    /**
     * 执行更新
     *
     * @return 影响的行数
     */
    public int update() throws SQLException {
        return dao.update(this);
    }

    /**
     * 构建 UPDATE SQL
     */
    public String buildSql() {
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
    public List<Object> getParameters() {
        List<Object> parameters = new ArrayList<>(setValues.values());
        where.collectParameters(parameters);
        for (int i = 0; i < parameters.size(); i++) {
            parameters.set(i, dialect.preprocessParameter(parameters.get(i)));
        }
        return parameters;
    }

}
