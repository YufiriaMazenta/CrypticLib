package crypticlib.database.dialect;

import crypticlib.database.annotation.Field;
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
        // replace 需要写入主键，否则无法匹配到已有记录；参数绑定顺序与 getColumns 一致
        List<ColumnInfo> columns = tableInfo.getColumns();
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

    /**
     * 列定义：列名 + 类型片段 + NOT NULL / UNIQUE / DEFAULT
     */
    @Override
    public String generateColumnDefinition(ColumnInfo columnInfo) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(quoteIdentifier(columnInfo.getColumnName()));
        sqlBuilder.append(" ");
        sqlBuilder.append(resolveColumnType(columnInfo));

        if (!columnInfo.isNullable()) {
            sqlBuilder.append(" NOT NULL");
        }

        if (columnInfo.isUnique()) {
            sqlBuilder.append(" UNIQUE");
        }

        if (!columnInfo.getDefaultValue().isEmpty()) {
            sqlBuilder.append(" DEFAULT ").append(columnInfo.getDefaultValue());
        }

        return sqlBuilder.toString();
    }

    /**
     * 解析列的完整类型片段（自增主键走方言的自增类型）
     */
    protected String resolveColumnType(ColumnInfo columnInfo) {
        if (columnInfo.isId() && columnInfo.isGenerated()) {
            return autoIncrementColumnType();
        }
        return resolveDataType(columnInfo);
    }

    /**
     * 自增主键的类型片段，不使用 AUTO_INCREMENT 关键字的方言需要覆盖
     */
    protected String autoIncrementColumnType() {
        return "BIGINT " + getAutoIncrementSql();
    }

    /**
     * 非自增列的列类型：显式声明的列类型优先，其次是显式声明的长度，最后按 Java 类型自动识别
     */
    protected String resolveDataType(ColumnInfo columnInfo) {
        if (columnInfo.getColumnType() != Field.ColumnType.AUTO) {
            return mapColumnType(columnInfo);
        }
        Class<?> javaType = columnInfo.getJavaType();
        int length = columnInfo.getLength();
        if (length > 0 && (javaType == String.class || javaType.isEnum())) {
            return "VARCHAR(" + length + ")";
        }
        return mapJavaType(javaType);
    }

    /**
     * 把显式声明的 {@link Field.ColumnType} 映射为 SQL 类型，方言覆盖此方法以适配差异（如 BYTEA、CLOB）
     */
    protected String mapColumnType(ColumnInfo columnInfo) {
        int length = columnInfo.getLength() > 0 ? columnInfo.getLength() : 255;
        switch (columnInfo.getColumnType()) {
            case VARCHAR:
                return "VARCHAR(" + length + ")";
            case TEXT:
                return getTextType();
            case TINYINT:
                return "TINYINT";
            case SMALLINT:
                return "SMALLINT";
            case INT:
                return "INTEGER";
            case BIGINT:
                return "BIGINT";
            case FLOAT:
                return "FLOAT";
            case DOUBLE:
                return "DOUBLE PRECISION";
            case DECIMAL:
                return "DECIMAL";
            case BOOLEAN:
                return getBooleanType();
            default:
                return mapJavaType(columnInfo.getJavaType());
        }
    }

    /**
     * 大文本类型的 SQL 名称，H2 等需要使用 CLOB 的方言需要覆盖
     */
    protected String getTextType() {
        return "TEXT";
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
