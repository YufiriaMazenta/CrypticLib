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
        return quoteWith(identifier, '"');
    }

    @Override
    public String getAutoIncrementSql() {
        return "AUTO_INCREMENT";
    }

    @Override
    public String appendLimitOffset(String sql, long limit, long offset) {
        if (limit < 0) return sql;
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
    protected String getTextType() {
        // H2 常规模式没有 TEXT 类型
        return "CLOB";
    }

}
