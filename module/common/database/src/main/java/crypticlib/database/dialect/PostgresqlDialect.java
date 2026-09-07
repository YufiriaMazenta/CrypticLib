package crypticlib.database.dialect;

import crypticlib.database.table.ColumnInfo;
import crypticlib.database.table.TableInfo;

import java.util.List;
import java.util.StringJoiner;

/**
 * PostgreSQL 方言
 */
public class PostgresqlDialect extends AbstractDialect {

    @Override
    public String quoteIdentifier(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public String getAutoIncrementSql() {
        return ""; // PostgreSQL 使用 SERIAL / BIGSERIAL 类型
    }

    @Override
    public String appendLimitOffset(String sql, long limit, long offset) {
        if (limit <= 0) return sql;
        StringBuilder sb = new StringBuilder(sql);
        sb.append(" LIMIT ").append(limit);
        if (offset > 0) {
            sb.append(" OFFSET ").append(offset);
        }
        return sb.toString();
    }

    @Override
    public String generateReplaceSql(TableInfo tableInfo) {
        ColumnInfo idColumn = tableInfo.getIdColumn();
        List<ColumnInfo> columns = tableInfo.getNonIdColumns();
        StringJoiner columnJoiner = new StringJoiner(", ");
        StringJoiner placeholderJoiner = new StringJoiner(", ");
        StringJoiner updateJoiner = new StringJoiner(", ");

        for (ColumnInfo column : columns) {
            columnJoiner.add(quoteIdentifier(column.getColumnName()));
            placeholderJoiner.add("?");
            updateJoiner.add(quoteIdentifier(column.getColumnName()) + " = EXCLUDED." + quoteIdentifier(column.getColumnName()));
        }

        return "INSERT INTO " + quoteIdentifier(tableInfo.getTableName())
            + " (" + columnJoiner + ") VALUES (" + placeholderJoiner + ")"
            + " ON CONFLICT (" + quoteIdentifier(idColumn.getColumnName()) + ") DO UPDATE SET " + updateJoiner;
    }

    @Override
    public String mapJavaType(Class<?> javaType) {
        if (javaType == long.class || javaType == Long.class) return "BIGINT";
        if (javaType == boolean.class || javaType == Boolean.class) return "BOOLEAN";
        return super.mapJavaType(javaType);
    }

    @Override
    public String generateColumnDefinition(ColumnInfo columnInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(quoteIdentifier(columnInfo.getColumnName()));
        sb.append(" ");

        if (columnInfo.isId() && columnInfo.isGenerated()) {
            sb.append("BIGSERIAL");
        } else {
            sb.append(mapJavaType(columnInfo.getJavaType()));
        }

        if (!columnInfo.isNullable()) {
            sb.append(" NOT NULL");
        }

        if (columnInfo.isUnique()) {
            sb.append(" UNIQUE");
        }

        if (!columnInfo.getDefaultValue().isEmpty()) {
            sb.append(" DEFAULT ").append(columnInfo.getDefaultValue());
        }

        return sb.toString();
    }

}
