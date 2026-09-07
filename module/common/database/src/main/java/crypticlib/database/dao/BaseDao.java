package crypticlib.database.dao;

import crypticlib.database.connection.ConnectionSource;
import crypticlib.database.dialect.DatabaseDialect;
import crypticlib.database.statement.DeleteBuilder;
import crypticlib.database.statement.QueryBuilder;
import crypticlib.database.statement.UpdateBuilder;
import crypticlib.database.table.ColumnInfo;
import crypticlib.database.table.TableInfo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DAO 基类实现
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

    @Override
    public T queryForId(Object id) throws SQLException {
        String sql = dialect.generateQueryByIdSql(tableInfo);
        Connection connection = connectionSource.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            setParameter(statement, 1, id, tableInfo.getIdColumn().getJavaType());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return mapResultSetToEntity(resultSet);
            }
            return null;
        } finally {
            connectionSource.releaseConnection(connection);
        }
    }

    @Override
    public List<T> queryForAll() throws SQLException {
        String sql = dialect.generateQueryAllSql(tableInfo);
        Connection connection = connectionSource.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(mapResultSetToEntity(resultSet));
            }
            return results;
        } finally {
            connectionSource.releaseConnection(connection);
        }
    }

    @Override
    public List<T> query(QueryBuilder<T> queryBuilder) throws SQLException {
        String sql = queryBuilder.buildSql();
        Connection connection = connectionSource.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            List<Object> parameters = queryBuilder.getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            ResultSet resultSet = statement.executeQuery();
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(mapResultSetToEntity(resultSet));
            }
            return results;
        } finally {
            connectionSource.releaseConnection(connection);
        }
    }

    @Override
    public int update(UpdateBuilder<T> updateBuilder) throws SQLException {
        String sql = updateBuilder.buildSql();
        Connection connection = connectionSource.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            List<Object> parameters = updateBuilder.getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            return statement.executeUpdate();
        } finally {
            connectionSource.releaseConnection(connection);
        }
    }

    @Override
    public int delete(DeleteBuilder<T> deleteBuilder) throws SQLException {
        String sql = deleteBuilder.buildSql();
        Connection connection = connectionSource.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            List<Object> parameters = deleteBuilder.getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            return statement.executeUpdate();
        } finally {
            connectionSource.releaseConnection(connection);
        }
    }

    @Override
    public int create(T entity) throws SQLException {
        String sql = dialect.generateInsertSql(tableInfo);
        Connection connection = connectionSource.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            List<ColumnInfo> nonIdColumns = tableInfo.getNonIdColumns();
            for (int i = 0; i < nonIdColumns.size(); i++) {
                Object value = nonIdColumns.get(i).getValue(entity);
                setParameter(statement, i + 1, value, nonIdColumns.get(i).getJavaType());
            }
            int result = statement.executeUpdate();

            // 设置自动生成的 ID
            ColumnInfo idColumn = tableInfo.getIdColumn();
            if (idColumn != null && idColumn.isGenerated()) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    Object generatedId = getGeneratedId(generatedKeys, idColumn.getJavaType());
                    idColumn.setValue(entity, generatedId);
                }
            }

            return result;
        } finally {
            connectionSource.releaseConnection(connection);
        }
    }

    @Override
    public int update(T entity) throws SQLException {
        String sql = dialect.generateUpdateSql(tableInfo);
        Connection connection = connectionSource.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
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
        } finally {
            connectionSource.releaseConnection(connection);
        }
    }

    @Override
    public int delete(T entity) throws SQLException {
        String sql = dialect.generateDeleteSql(tableInfo);
        Connection connection = connectionSource.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            ColumnInfo idColumn = tableInfo.getIdColumn();
            Object idValue = idColumn.getValue(entity);
            setParameter(statement, 1, idValue, idColumn.getJavaType());
            return statement.executeUpdate();
        } finally {
            connectionSource.releaseConnection(connection);
        }
    }

    @Override
    public int replace(T entity) throws SQLException {
        ColumnInfo idColumn = tableInfo.getIdColumn();
        // 自增主键且未赋值时，走普通 INSERT
        if (idColumn != null && idColumn.isGenerated()) {
            Object idValue = idColumn.getValue(entity);
            if (idValue instanceof Number && ((Number) idValue).longValue() == 0) {
                return create(entity);
            }
        }
        String sql = dialect.generateReplaceSql(tableInfo);
        Connection connection = connectionSource.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            List<ColumnInfo> columns = tableInfo.getColumns();
            for (int i = 0; i < columns.size(); i++) {
                Object value = columns.get(i).getValue(entity);
                setParameter(statement, i + 1, value, columns.get(i).getJavaType());
            }
            return statement.executeUpdate();
        } finally {
            connectionSource.releaseConnection(connection);
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
     * 设置 PreparedStatement 参数
     */
    private void setParameter(PreparedStatement statement, int index, Object value, Class<?> javaType) throws SQLException {
        if (value == null) {
            statement.setNull(index, getSqlType(javaType));
            return;
        }

        // 类型转换
        Object converted = convertValue(value, javaType);

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
        } else if (javaType == UUID.class) {
            statement.setString(index, converted.toString());
        } else if (javaType.isEnum()) {
            statement.setString(index, ((Enum<?>) converted).name());
        } else {
            statement.setObject(index, converted);
        }
    }

    /**
     * 值类型转换
     */
    @SuppressWarnings("unchecked")
    private Object convertValue(Object value, Class<?> targetType) {
        if (targetType.isInstance(value)) return value;

        // Number 类型互转
        if (value instanceof Number) {
            Number num = (Number) value;
            if (targetType == long.class || targetType == Long.class) return num.longValue();
            if (targetType == int.class || targetType == Integer.class) return num.intValue();
            if (targetType == double.class || targetType == Double.class) return num.doubleValue();
            if (targetType == float.class || targetType == Float.class) return num.floatValue();
            if (targetType == byte.class || targetType == Byte.class) return num.byteValue();
            if (targetType == short.class || targetType == Short.class) return num.shortValue();
        }

        // String -> Enum
        if (targetType.isEnum() && value instanceof String) {
            return Enum.valueOf((Class<Enum>) targetType, (String) value);
        }

        // Enum -> String
        if (targetType == String.class && value instanceof Enum) {
            return ((Enum<?>) value).name();
        }

        return value;
    }

    /**
     * 获取生成的 ID
     */
    private Object getGeneratedId(ResultSet resultSet, Class<?> javaType) throws SQLException {
        if (javaType == long.class || javaType == Long.class) {
            return resultSet.getLong(1);
        } else if (javaType == int.class || javaType == Integer.class) {
            return resultSet.getInt(1);
        } else {
            return resultSet.getObject(1);
        }
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
        return Types.VARCHAR;
    }

}
