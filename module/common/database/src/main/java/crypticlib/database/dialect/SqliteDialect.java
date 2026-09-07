package crypticlib.database.dialect;

import crypticlib.database.table.ColumnInfo;

/**
 * SQLite 方言
 */
public class SqliteDialect extends AbstractDialect {

    @Override
    public String quoteIdentifier(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public String getAutoIncrementSql() {
        return "AUTOINCREMENT";
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
    public String getBooleanType() {
        return "INTEGER";
    }

    @Override
    public String generateColumnDefinition(ColumnInfo columnInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(quoteIdentifier(columnInfo.getColumnName()));
        sb.append(" ");

        if (columnInfo.isId() && columnInfo.isGenerated()) {
            sb.append("INTEGER");
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

        if (columnInfo.isId() && columnInfo.isGenerated()) {
            sb.append(" ").append(getAutoIncrementSql());
        }

        return sb.toString();
    }

}
