package crypticlib.database.dialect;

import crypticlib.database.table.ColumnInfo;
import crypticlib.database.table.TableInfo;

import java.util.List;
import java.util.StringJoiner;

/**
 * MySQL / MariaDB 方言
 */
public class MysqlDialect extends AbstractDialect {

    @Override
    public String quoteIdentifier(String identifier) {
        return "`" + identifier + "`";
    }

    @Override
    public String getAutoIncrementSql() {
        return "AUTO_INCREMENT";
    }

    @Override
    public String appendLimitOffset(String sql, long limit, long offset) {
        if (limit <= 0) return sql;
        StringBuilder sb = new StringBuilder(sql);
        if (offset > 0) {
            sb.append(" LIMIT ").append(offset).append(", ").append(limit);
        } else {
            sb.append(" LIMIT ").append(limit);
        }
        return sb.toString();
    }

    @Override
    public String generateReplaceSql(TableInfo tableInfo) {
        List<ColumnInfo> columns = tableInfo.getNonIdColumns();
        StringJoiner columnJoiner = new StringJoiner(", ");
        StringJoiner placeholderJoiner = new StringJoiner(", ");
        StringJoiner updateJoiner = new StringJoiner(", ");

        for (ColumnInfo column : columns) {
            columnJoiner.add(quoteIdentifier(column.getColumnName()));
            placeholderJoiner.add("?");
            updateJoiner.add(quoteIdentifier(column.getColumnName()) + " = VALUES(" + quoteIdentifier(column.getColumnName()) + ")");
        }

        return "INSERT INTO " + quoteIdentifier(tableInfo.getTableName())
            + " (" + columnJoiner + ") VALUES (" + placeholderJoiner + ")"
            + " ON DUPLICATE KEY UPDATE " + updateJoiner;
    }

    @Override
    public String getBooleanType() {
        return "TINYINT(1)";
    }

    @Override
    public String generateColumnDefinition(ColumnInfo columnInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(quoteIdentifier(columnInfo.getColumnName()));
        sb.append(" ");

        if (columnInfo.isId() && columnInfo.isGenerated()) {
            sb.append("BIGINT");
        } else {
            sb.append(mapJavaType(columnInfo.getJavaType()));
        }

        if (!columnInfo.isNullable()) {
            sb.append(" NOT NULL");
        }

        if (columnInfo.isUnique()) {
            sb.append(" UNIQUE");
        }

        if (columnInfo.isId() && columnInfo.isGenerated()) {
            sb.append(" ").append(getAutoIncrementSql());
        }

        if (!columnInfo.getDefaultValue().isEmpty()) {
            sb.append(" DEFAULT ").append(columnInfo.getDefaultValue());
        }

        return sb.toString();
    }

}
