package crypticlib.database.dialect;

import crypticlib.database.annotation.Field;
import crypticlib.database.table.ColumnInfo;
import crypticlib.database.table.TableInfo;

import java.math.BigDecimal;
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
        // 列集合与 DAO 绑定参数的列集合必须同为 getInsertColumns：自增主键排除、非自增主键参与插入
        List<ColumnInfo> columns = tableInfo.getInsertColumns();
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
        return coerceValue(value, null);
    }

    /**
     * 值类型转换的唯一入口，实体路径（已知列的 Java 类型）与构建器路径（未知目标类型）共用
     *
     * @param value      原始值
     * @param targetType 目标 Java 类型；为 null 时只做「转成 JDBC 可直接绑定的类型」的转换
     * @return 转换后的值
     */
    @SuppressWarnings("unchecked")
    public static Object coerceValue(Object value, Class<?> targetType) {
        if (value == null) return null;

        // 构建器路径：没有目标类型，只把 UUID/枚举转成字符串，Number 交给驱动处理
        if (targetType == null) {
            if (value instanceof UUID) return value.toString();
            if (value instanceof Enum) return ((Enum<?>) value).name();
            return value;
        }

        if (targetType.isInstance(value)) return value;

        // UUID 与 String 互转
        if (targetType == String.class && value instanceof UUID) return value.toString();
        if (targetType == UUID.class && value instanceof String) return UUID.fromString((String) value);

        // Number 类型互转
        if (value instanceof Number) {
            Number num = (Number) value;
            if (targetType == long.class || targetType == Long.class) return num.longValue();
            if (targetType == int.class || targetType == Integer.class) return num.intValue();
            if (targetType == double.class || targetType == Double.class) return num.doubleValue();
            if (targetType == float.class || targetType == Float.class) return num.floatValue();
            if (targetType == byte.class || targetType == Byte.class) return num.byteValue();
            if (targetType == short.class || targetType == Short.class) return num.shortValue();
            // 用字符串构造，避免 double 的二进制误差被带进来
            if (targetType == BigDecimal.class) return new BigDecimal(num.toString());
            if (targetType == boolean.class || targetType == Boolean.class) return num.longValue() != 0;
        }

        // Boolean 与 Number 互转
        if (value instanceof Boolean) {
            Boolean bool = (Boolean) value;
            if (targetType == long.class || targetType == Long.class) return bool ? 1L : 0L;
            if (targetType == int.class || targetType == Integer.class) return bool ? 1 : 0;
            if (targetType == double.class || targetType == Double.class) return bool ? 1D : 0D;
            if (targetType == float.class || targetType == Float.class) return bool ? 1F : 0F;
            if (targetType == byte.class || targetType == Byte.class) return (byte) (bool ? 1 : 0);
            if (targetType == short.class || targetType == Short.class) return (short) (bool ? 1 : 0);
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

    @Override
    public String getBooleanType() {
        return "BOOLEAN";
    }

    /**
     * 引用标识符，并把标识符内部的引号字符双写转义
     * <p>
     * 不做转义时，名字里带引号的表名/列名会提前结束引用，拼接出的 SQL 语义被改变
     *
     * @param identifier 原始标识符
     * @param quoteChar  该方言的引用字符（`、" 等）
     * @return 可安全拼接进 SQL 的引用标识符
     */
    protected static String quoteWith(String identifier, char quoteChar) {
        StringBuilder builder = new StringBuilder(identifier.length() + 2);
        builder.append(quoteChar);
        for (int i = 0; i < identifier.length(); i++) {
            char ch = identifier.charAt(i);
            if (ch == quoteChar) {
                builder.append(quoteChar);
            }
            builder.append(ch);
        }
        builder.append(quoteChar);
        return builder.toString();
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
        return mapJavaType(columnInfo);
    }

    /**
     * 自增主键的类型片段，不使用 AUTO_INCREMENT 关键字的方言需要覆盖
     */
    protected String autoIncrementColumnType() {
        return "BIGINT " + getAutoIncrementSql();
    }

    /**
     * 把显式声明的 {@link Field.ColumnType} 映射为 SQL 类型，方言覆盖此方法以适配差异（如 CLOB）
     */
    protected String mapColumnType(ColumnInfo columnInfo) {
        int length = columnInfo.getLength() > 0 ? columnInfo.getLength() : 255;
        switch (columnInfo.getColumnType()) {
            case VARCHAR:
                return "VARCHAR(" + length + ")";
            case TEXT:
                return getTextType();
            case TINYINT:
                return getTinyIntType();
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
                return getDecimalType();
            case BOOLEAN:
                return getBooleanType();
            default:
                throw new IllegalStateException("Unhandled column type: " + columnInfo.getColumnType());
        }
    }

    /**
     * 定点数类型，精度与小数位是通用约定；需要自定义精度时后续再提供声明入口
     */
    protected String getDecimalType() {
        return "DECIMAL(65, 30)";
    }

    /**
     * 大文本类型的 SQL 名称，H2 等需要使用 CLOB 的方言需要覆盖
     */
    protected String getTextType() {
        return "TEXT";
    }

    /**
     * 微整型类型的 SQL 名称，PostgreSQL 没有 TINYINT，需要覆盖为 SMALLINT
     */
    protected String getTinyIntType() {
        return "TINYINT";
    }

    /**
     * 标准分页语法（{@code LIMIT n OFFSET m}），H2 / SQLite / PostgreSQL 通用；
     * MySQL 使用 {@code LIMIT offset, limit}，由自身覆盖
     */
    @Override
    public String appendLimitOffset(String sql, long limit, long offset) {
        if (limit < 0) return sql;
        StringBuilder builder = new StringBuilder(sql);
        builder.append(" LIMIT ").append(limit);
        if (offset > 0) {
            builder.append(" OFFSET ").append(offset);
        }
        return builder.toString();
    }

    /**
     * 列的 SQL 类型映射：显式声明的列类型优先，其次是声明的长度，最后按 Java 类型默认映射。
     * 无法识别的 Java 类型直接抛异常，避免静默建出一列字符串
     */
    @Override
    public String mapJavaType(ColumnInfo columnInfo) {
        if (columnInfo.getColumnType() != Field.ColumnType.AUTO) {
            return mapColumnType(columnInfo);
        }

        Class<?> javaType = columnInfo.getJavaType();
        int length = columnInfo.getLength();
        if (length > 0 && (javaType == String.class || javaType.isEnum())) {
            return "VARCHAR(" + length + ")";
        }

        if (javaType == String.class) return "VARCHAR(255)";
        if (javaType == int.class || javaType == Integer.class) return "INTEGER";
        if (javaType == long.class || javaType == Long.class) return "BIGINT";
        if (javaType == float.class || javaType == Float.class) return "FLOAT";
        if (javaType == double.class || javaType == Double.class) return "DOUBLE PRECISION";
        if (javaType == boolean.class || javaType == Boolean.class) return getBooleanType();
        if (javaType == byte.class || javaType == Byte.class) return getTinyIntType();
        if (javaType == short.class || javaType == Short.class) return "SMALLINT";
        if (javaType == BigDecimal.class) return getDecimalType();
        if (javaType == UUID.class) return "VARCHAR(36)";
        if (javaType.isEnum()) return "VARCHAR(" + maxEnumNameLength(javaType) + ")";

        throw new IllegalArgumentException("Java type " + javaType.getName() + " of field "
            + columnInfo.getField().getName() + " has no default column type mapping, declare it with @Field(type = ...)");
    }

    /**
     * 枚举列的默认长度：取所有常量名长度的最大值，至少 1
     */
    protected int maxEnumNameLength(Class<?> enumType) {
        Object[] constants = enumType.getEnumConstants();
        if (constants == null || constants.length == 0) {
            return 1;
        }
        int maxLength = 1;
        for (Object constant : constants) {
            int length = ((Enum<?>) constant).name().length();
            if (length > maxLength) {
                maxLength = length;
            }
        }
        return maxLength;
    }

}
