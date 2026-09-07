package crypticlib.database.dialect;

import crypticlib.database.table.ColumnInfo;
import crypticlib.database.table.TableInfo;

import java.util.List;
import java.util.StringJoiner;

/**
 * H2 方言
 */
public class H2Dialect extends AbstractDialect {

    @Override
    public String quoteIdentifier(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public String getAutoIncrementSql() {
        return "AUTO_INCREMENT";
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
        // H2 使用 MERGE INTO，需要包含所有列（包括主键）
        ColumnInfo idColumn = tableInfo.getIdColumn();
        List<ColumnInfo> columns = tableInfo.getColumns();
        StringJoiner columnJoiner = new StringJoiner(", ");
        StringJoiner placeholderJoiner = new StringJoiner(", ");

        for (ColumnInfo column : columns) {
            columnJoiner.add(quoteIdentifier(column.getColumnName()));
            placeholderJoiner.add("?");
        }

        return "MERGE INTO " + quoteIdentifier(tableInfo.getTableName())
            + " (" + columnJoiner + ") KEY (" + quoteIdentifier(idColumn.getColumnName()) + ") VALUES (" + placeholderJoiner + ")";
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
