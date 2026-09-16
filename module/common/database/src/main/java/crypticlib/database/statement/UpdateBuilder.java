package crypticlib.database.statement;

import crypticlib.database.connection.ConnectionSource;
import crypticlib.database.dao.Dao;
import crypticlib.database.dialect.AbstractDialect;
import crypticlib.database.dialect.DatabaseDialect;
import crypticlib.database.table.ColumnInfo;
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
     * <p>
     * 值为 null 表示把列更新为 NULL；列名会与实体的列元数据校验
     */
    public UpdateBuilder<T> set(String column, Object value) {
        ColumnInfo columnInfo = tableInfo.requireColumn(column);
        setValues.put(column, AbstractDialect.coerceValue(value, columnInfo.getJavaType()));
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
     * <p>
     * 没有 SET 列或没有 WHERE 条件都会抛异常：前者生成的 SQL 无法执行，
     * 后者会更新整张表，属于误用而不是调用方的本意
     */
    public String buildSql() {
        if (setValues.isEmpty()) {
            throw new IllegalStateException("No column to update, call set(column, value) at least once");
        }
        if (where.isEmpty()) {
            throw new IllegalStateException("Refusing to update every row of table \""
                + tableInfo.getTableName() + "\", add a where condition");
        }

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
