package crypticlib.database.dao;

import crypticlib.database.connection.ConnectionSource;
import crypticlib.database.dialect.AbstractDialect;
import crypticlib.database.dialect.DatabaseDialect;
import crypticlib.database.statement.DeleteBuilder;
import crypticlib.database.statement.QueryBuilder;
import crypticlib.database.statement.UpdateBuilder;
import crypticlib.database.table.ColumnInfo;
import crypticlib.database.table.TableInfo;
import crypticlib.database.transaction.TransactionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DAO 基类实现
 * <p>
 * 所有方法都提供两个版本：不带连接时自行向 ConnectionSource 借用连接并归还；
 * 带 Connection 参数时使用调用方传入的连接（用于事务中复用同一条连接）。
 * <p>
 * 不带连接的版本在检测到当前线程正处于该连接源的事务中时会直接抛 IllegalStateException（快速失败）：
 * 它们借到的是另一条连接、写入会自动提交，静默放行只会让调用方误以为操作在事务里。
 *
 * @param <T>  实体类型
 */
public class BaseDao<T> implements Dao<T> {

    private final ConnectionSource connectionSource;
    private final Class<T> entityClass;
    private final TableInfo tableInfo;
    private final DatabaseDialect dialect;

    public BaseDao(ConnectionSource connectionSource, Class<T> entityClass) {
        this.connectionSource = connectionSource;
        this.entityClass = entityClass;
        this.tableInfo = TableInfo.of(entityClass);
        this.dialect = connectionSource.getDialect();
    }

    /**
     * 借用连接，处于事务中时快速失败
     */
    private Connection borrowConnection() throws SQLException {
        if (TransactionManager.isInTransaction(connectionSource)) {
            throw new IllegalStateException("Cannot use the Connection-less method inside a transaction, "
                + "use the overload that takes the transaction Connection instead, otherwise the operation "
                + "would run on another connection and escape the transaction");
        }
        return connectionSource.getConnection();
    }

    @Override
    public T queryForId(Object id) throws SQLException {
        Connection connection = borrowConnection();
        try {
            return queryForId(connection, id);
        } finally {
            connectionSource.releaseConnection(connection);
        }
    }

    @Override
    public T queryForId(Connection connection, Object id) throws SQLException {
        String sql = dialect.generateQueryByIdSql(tableInfo);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            setParameter(statement, 1, id, tableInfo.getIdColumn().getJavaType());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToEntity(resultSet);
                }
                return null;
            }
        }
    }

    @Override
    public List<T> queryForAll() throws SQLException {
        Connection connection = borrowConnection();
        try {
            return queryForAll(connection);
        } finally {
            connectionSource.releaseConnection(connection);
        }
    }

    @Override
    public List<T> queryForAll(Connection connection) throws SQLException {
        String sql = dialect.generateQueryAllSql(tableInfo);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(mapResultSetToEntity(resultSet));
                }
                return results;
            }
        }
    }

    @Override
    public List<T> query(QueryBuilder<T> queryBuilder) throws SQLException {
        Connection connection = borrowConnection();
        try {
            return query(connection, queryBuilder);
        } finally {
            connectionSource.releaseConnection(connection);
        }
    }

    @Override
    public List<T> query(Connection connection, QueryBuilder<T> queryBuilder) throws SQLException {
        String sql = queryBuilder.buildSql();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindParameters(statement, queryBuilder.getParameters());
            try (ResultSet resultSet = statement.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(mapResultSetToEntity(resultSet));
                }
                return results;
            }
        }
    }

    @Override
    public int update(UpdateBuilder<T> updateBuilder) throws SQLException {
        Connection connection = borrowConnection();
        try {
            return update(connection, updateBuilder);
        } finally {
            connectionSource.releaseConnection(connection);
        }
    }

    @Override
    public int update(Connection connection, UpdateBuilder<T> updateBuilder) throws SQLException {
        String sql = updateBuilder.buildSql();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindParameters(statement, updateBuilder.getParameters());
            return statement.executeUpdate();
        }
    }

    @Override
    public int delete(DeleteBuilder<T> deleteBuilder) throws SQLException {
        Connection connection = borrowConnection();
        try {
            return delete(connection, deleteBuilder);
        } finally {
            connectionSource.releaseConnection(connection);
        }
    }

    @Override
    public int delete(Connection connection, DeleteBuilder<T> deleteBuilder) throws SQLException {
        String sql = deleteBuilder.buildSql();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindParameters(statement, deleteBuilder.getParameters());
            return statement.executeUpdate();
        }
    }

    @Override
    public int create(T entity) throws SQLException {
        Connection connection = borrowConnection();
        try {
            return create(connection, entity);
        } finally {
            connectionSource.releaseConnection(connection);
        }
    }

    @Override
    public int create(Connection connection, T entity) throws SQLException {
        String sql = dialect.generateInsertSql(tableInfo);
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // 与 generateInsertSql 使用同一份列集合：自增主键不写入，非自增主键必须写入
            List<ColumnInfo> insertColumns = tableInfo.getInsertColumns();
            for (int i = 0; i < insertColumns.size(); i++) {
                Object value = insertColumns.get(i).getValue(entity);
                setParameter(statement, i + 1, value, insertColumns.get(i).getJavaType());
            }
            int result = statement.executeUpdate();

            // 设置自动生成的 ID
            ColumnInfo idColumn = tableInfo.getIdColumn();
            if (idColumn.isGenerated()) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Object generatedId = getGeneratedId(generatedKeys, idColumn);
                        idColumn.setValue(entity, generatedId);
                    }
                }
            }

            return result;
        }
    }

    @Override
    public int update(T entity) throws SQLException {
        Connection connection = borrowConnection();
        try {
            return update(connection, entity);
        } finally {
            connectionSource.releaseConnection(connection);
        }
    }

    @Override
    public int update(Connection connection, T entity) throws SQLException {
        String sql = dialect.generateUpdateSql(tableInfo);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            List<ColumnInfo> nonIdColumns = tableInfo.getNonIdColumns();
            ColumnInfo idColumn = tableInfo.getIdColumn();
            int parameterIndex = 1;

            // SET 子句的参数
            for (ColumnInfo column : nonIdColumns) {
                Object value = column.getValue(entity);
                setParameter(statement, parameterIndex++, value, column.getJavaType());
            }

            // WHERE 子句的参数（主键）
            Object idValue = idColumn.getValue(entity);
            setParameter(statement, parameterIndex, idValue, idColumn.getJavaType());

            return statement.executeUpdate();
        }
    }

    @Override
    public int delete(T entity) throws SQLException {
        Connection connection = borrowConnection();
        try {
            return delete(connection, entity);
        } finally {
            connectionSource.releaseConnection(connection);
        }
    }

    @Override
    public int delete(Connection connection, T entity) throws SQLException {
        String sql = dialect.generateDeleteSql(tableInfo);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ColumnInfo idColumn = tableInfo.getIdColumn();
            Object idValue = idColumn.getValue(entity);
            setParameter(statement, 1, idValue, idColumn.getJavaType());
            return statement.executeUpdate();
        }
    }

    @Override
    public int replace(T entity) throws SQLException {
        Connection connection = borrowConnection();
        try {
            return replace(connection, entity);
        } finally {
            connectionSource.releaseConnection(connection);
        }
    }

    @Override
    public int replace(Connection connection, T entity) throws SQLException {
        ColumnInfo idColumn = tableInfo.getIdColumn();
        // 自增主键没有值（null 或 0）时，走普通 INSERT 让数据库生成主键
        if (idColumn.isGenerated()) {
            Object idValue = idColumn.getValue(entity);
            if (idValue == null || (idValue instanceof Number && ((Number) idValue).longValue() == 0)) {
                return create(connection, entity);
            }
        }

        String sql = dialect.generateReplaceSql(tableInfo);
        List<ColumnInfo> columns = tableInfo.getColumns();
        int placeholderCount = countPlaceholders(sql);
        if (placeholderCount != columns.size()) {
            throw new IllegalStateException("Placeholder count (" + placeholderCount
                + ") of the replace statement does not match the bound column count (" + columns.size() + "): " + sql);
        }

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < columns.size(); i++) {
                Object value = columns.get(i).getValue(entity);
                setParameter(statement, i + 1, value, columns.get(i).getJavaType());
            }
            return statement.executeUpdate();
        }
    }

    @Override
    public QueryBuilder<T> queryBuilder() {
        return new QueryBuilder<>(this, connectionSource, tableInfo);
    }

    @Override
    public UpdateBuilder<T> updateBuilder() {
        return new UpdateBuilder<>(this, connectionSource, tableInfo);
    }

    @Override
    public DeleteBuilder<T> deleteBuilder() {
        return new DeleteBuilder<>(this, connectionSource, tableInfo);
    }

    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    public ConnectionSource getConnectionSource() {
        return connectionSource;
    }

    public TableInfo getTableInfo() {
        return tableInfo;
    }

    /**
     * 将 ResultSet 映射到实体对象
     */
    @SuppressWarnings("unchecked")
    private T mapResultSetToEntity(ResultSet resultSet) throws SQLException {
        T entity = (T) tableInfo.newInstance();
        for (ColumnInfo column : tableInfo.getColumns()) {
            Object value = getColumnValue(resultSet, column);
            if (value != null) {
                column.setValue(entity, value);
            }
        }
        return entity;
    }

    /**
     * 从 ResultSet 获取列值
     */
    private Object getColumnValue(ResultSet resultSet, ColumnInfo column) throws SQLException {
        String columnName = column.getColumnName();
        Class<?> javaType = column.getJavaType();

        if (javaType == String.class) {
            return resultSet.getString(columnName);
        } else if (javaType == int.class || javaType == Integer.class) {
            int value = resultSet.getInt(columnName);
            return resultSet.wasNull() ? null : value;
        } else if (javaType == long.class || javaType == Long.class) {
            long value = resultSet.getLong(columnName);
            return resultSet.wasNull() ? null : value;
        } else if (javaType == float.class || javaType == Float.class) {
            float value = resultSet.getFloat(columnName);
            return resultSet.wasNull() ? null : value;
        } else if (javaType == double.class || javaType == Double.class) {
            double value = resultSet.getDouble(columnName);
            return resultSet.wasNull() ? null : value;
        } else if (javaType == boolean.class || javaType == Boolean.class) {
            boolean value = resultSet.getBoolean(columnName);
            return resultSet.wasNull() ? null : value;
        } else if (javaType == byte.class || javaType == Byte.class) {
            byte value = resultSet.getByte(columnName);
            return resultSet.wasNull() ? null : value;
        } else if (javaType == short.class || javaType == Short.class) {
            short value = resultSet.getShort(columnName);
            return resultSet.wasNull() ? null : value;
        } else if (javaType == BigDecimal.class) {
            return resultSet.getBigDecimal(columnName);
        } else if (javaType == UUID.class) {
            String value = resultSet.getString(columnName);
            return value != null ? UUID.fromString(value) : null;
        } else if (javaType.isEnum()) {
            String value = resultSet.getString(columnName);
            return value != null ? Enum.valueOf((Class<Enum>) javaType, value) : null;
        } else {
            return resultSet.getObject(columnName);
        }
    }

    /**
     * 绑定 PreparedStatement 参数（用于构建器生成的语句）
     */
    private void bindParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            statement.setObject(i + 1, parameters.get(i));
        }
    }

    /**
     * 统计 SQL 中的参数占位符数量
     */
    private int countPlaceholders(String sql) {
        int count = 0;
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?') {
                count++;
            }
        }
        return count;
    }

    /**
     * 设置 PreparedStatement 参数
     */
    private void setParameter(PreparedStatement statement, int index, Object value, Class<?> javaType) throws SQLException {
        if (value == null) {
            statement.setNull(index, getSqlType(javaType));
            return;
        }

        // 转换逻辑与方言的 preprocessParameter 共用唯一入口
        Object converted = AbstractDialect.coerceValue(value, javaType);

        if (javaType == String.class) {
            statement.setString(index, (String) converted);
        } else if (javaType == int.class || javaType == Integer.class) {
            statement.setInt(index, ((Number) converted).intValue());
        } else if (javaType == long.class || javaType == Long.class) {
            statement.setLong(index, ((Number) converted).longValue());
        } else if (javaType == float.class || javaType == Float.class) {
            statement.setFloat(index, ((Number) converted).floatValue());
        } else if (javaType == double.class || javaType == Double.class) {
            statement.setDouble(index, ((Number) converted).doubleValue());
        } else if (javaType == boolean.class || javaType == Boolean.class) {
            statement.setBoolean(index, (Boolean) converted);
        } else if (javaType == byte.class || javaType == Byte.class) {
            statement.setByte(index, ((Number) converted).byteValue());
        } else if (javaType == short.class || javaType == Short.class) {
            statement.setShort(index, ((Number) converted).shortValue());
        } else if (javaType == BigDecimal.class) {
            statement.setBigDecimal(index, (BigDecimal) converted);
        } else if (javaType == UUID.class) {
            statement.setString(index, converted.toString());
        } else if (javaType.isEnum()) {
            statement.setString(index, ((Enum<?>) converted).name());
        } else {
            statement.setObject(index, converted);
        }
    }

    /**
     * 获取生成的 ID
     * <p>
     * 结果集里只有一列时直接取第一列（H2/MySQL/SQLite 的驱动都只返回生成的键）；
     * PostgreSQL 的驱动在 RETURN_GENERATED_KEYS 下返回的是 {@code RETURNING *}（整行），
     * 主键不一定是第一列，因此能按主键列名匹配时一律按列名取值
     */
    private Object getGeneratedId(ResultSet generatedKeys, ColumnInfo idColumn) throws SQLException {
        String columnLabel = findColumnLabel(generatedKeys, idColumn.getColumnName());
        Class<?> javaType = idColumn.getJavaType();
        if (javaType == long.class || javaType == Long.class) {
            return columnLabel != null ? generatedKeys.getLong(columnLabel) : generatedKeys.getLong(1);
        } else if (javaType == int.class || javaType == Integer.class) {
            return columnLabel != null ? generatedKeys.getInt(columnLabel) : generatedKeys.getInt(1);
        }
        return columnLabel != null ? generatedKeys.getObject(columnLabel) : generatedKeys.getObject(1);
    }

    /**
     * 按列名（忽略大小写）在结果集中查找实际的列标签，找不到返回 null
     */
    private String findColumnLabel(ResultSet resultSet, String columnName) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String label = metaData.getColumnLabel(i);
            if (label != null && label.equalsIgnoreCase(columnName)) {
                return label;
            }
        }
        return null;
    }

    /**
     * 获取 SQL 类型
     */
    private int getSqlType(Class<?> javaType) {
        if (javaType == String.class) return Types.VARCHAR;
        if (javaType == int.class || javaType == Integer.class) return Types.INTEGER;
        if (javaType == long.class || javaType == Long.class) return Types.BIGINT;
        if (javaType == float.class || javaType == Float.class) return Types.FLOAT;
        if (javaType == double.class || javaType == Double.class) return Types.DOUBLE;
        if (javaType == boolean.class || javaType == Boolean.class) return Types.BOOLEAN;
        if (javaType == byte.class || javaType == Byte.class) return Types.TINYINT;
        if (javaType == short.class || javaType == Short.class) return Types.SMALLINT;
        if (javaType == BigDecimal.class) return Types.DECIMAL;
        return Types.VARCHAR;
    }

}
