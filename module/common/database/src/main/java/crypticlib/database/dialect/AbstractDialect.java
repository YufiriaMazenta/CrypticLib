package crypticlib.database.dialect;

import crypticlib.database.table.ColumnInfo;
import crypticlib.database.table.TableInfo;

import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * 方言基类，提供通用实现
 */
public abstract class AbstractDialect implements DatabaseDialect {

    @Override
    public String generateCreateTableSql(TableInfo tableInfo) {
        StringBuilder sqlBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sqlBuilder.append(quoteIdentifier(tableInfo.getTableName()));
        sqlBuilder.append(" (");

        List<ColumnInfo> columns = tableInfo.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sqlBuilder.append(", ");
            sqlBuilder.append(generateColumnDefinition(columns.get(i)));
        }

        // 主键约束
        ColumnInfo idColumn = tableInfo.getIdColumn();
        if (idColumn != null) {
            sqlBuilder.append(", PRIMARY KEY (");
            sqlBuilder.append(quoteIdentifier(idColumn.getColumnName()));
            sqlBuilder.append(")");
        }

        sqlBuilder.append(")");
        return sqlBuilder.toString();
    }

    @Override
    public String generateInsertSql(TableInfo tableInfo) {
        List<ColumnInfo> columns = tableInfo.getNonIdColumns();
        StringJoiner columnJoiner = new StringJoiner(", ");
        StringJoiner placeholderJoiner = new StringJoiner(", ");

        for (ColumnInfo column : columns) {
            columnJoiner.add(quoteIdentifier(column.getColumnName()));
            placeholderJoiner.add("?");
        }

        return "INSERT INTO " + quoteIdentifier(tableInfo.getTableName())
            + " (" + columnJoiner + ") VALUES (" + placeholderJoiner + ")";
    }

    @Override
    public String generateUpdateSql(TableInfo tableInfo) {
        List<ColumnInfo> columns = tableInfo.getNonIdColumns();
        ColumnInfo idColumn = tableInfo.getIdColumn();

        StringJoiner setJoiner = new StringJoiner(", ");
        for (ColumnInfo column : columns) {
            setJoiner.add(quoteIdentifier(column.getColumnName()) + " = ?");
        }

        return "UPDATE " + quoteIdentifier(tableInfo.getTableName())
            + " SET " + setJoiner
            + " WHERE " + quoteIdentifier(idColumn.getColumnName()) + " = ?";
    }

    @Override
    public String generateDeleteSql(TableInfo tableInfo) {
        ColumnInfo idColumn = tableInfo.getIdColumn();
        return "DELETE FROM " + quoteIdentifier(tableInfo.getTableName())
            + " WHERE " + quoteIdentifier(idColumn.getColumnName()) + " = ?";
    }

    @Override
    public String generateQueryByIdSql(TableInfo tableInfo) {
        ColumnInfo idColumn = tableInfo.getIdColumn();
        return "SELECT * FROM " + quoteIdentifier(tableInfo.getTableName())
            + " WHERE " + quoteIdentifier(idColumn.getColumnName()) + " = ?";
    }

    @Override
    public String generateQueryAllSql(TableInfo tableInfo) {
        return "SELECT * FROM " + quoteIdentifier(tableInfo.getTableName());
    }

    @Override
    public String generateReplaceSql(TableInfo tableInfo) {
        List<ColumnInfo> columns = tableInfo.getNonIdColumns();
        StringJoiner columnJoiner = new StringJoiner(", ");
        StringJoiner placeholderJoiner = new StringJoiner(", ");

        for (ColumnInfo column : columns) {
            columnJoiner.add(quoteIdentifier(column.getColumnName()));
            placeholderJoiner.add("?");
        }

        return "INSERT OR REPLACE INTO " + quoteIdentifier(tableInfo.getTableName())
            + " (" + columnJoiner + ") VALUES (" + placeholderJoiner + ")";
    }

    @Override
    public Object preprocessParameter(Object value) {
        if (value == null) return null;

        // UUID -> String
        if (value instanceof UUID) return value.toString();

        // Enum -> String
        if (value instanceof Enum) return ((Enum<?>) value).name();

        // Number 类型互转
        if (value instanceof Number) {
            // Number 类型直接返回，JDBC 驱动会处理
            return value;
        }

        return value;
    }

    @Override
    public String getBooleanType() {
        return "BOOLEAN";
    }

    @Override
    public String mapJavaType(Class<?> javaType) {
        if (javaType == String.class) return "VARCHAR(255)";
        if (javaType == int.class || javaType == Integer.class) return "INTEGER";
        if (javaType == long.class || javaType == Long.class) return "BIGINT";
        if (javaType == float.class || javaType == Float.class) return "FLOAT";
        if (javaType == double.class || javaType == Double.class) return "DOUBLE PRECISION";
        if (javaType == boolean.class || javaType == Boolean.class) return getBooleanType();
        if (javaType == byte.class || javaType == Byte.class) return "TINYINT";
        if (javaType == short.class || javaType == Short.class) return "SMALLINT";
        if (javaType == UUID.class) return "VARCHAR(36)";
        if (javaType.isEnum()) return "VARCHAR(255)";
        return "VARCHAR(255)";
    }

}
