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

    /**
     * 显式指定存储引擎与字符集：
     * 服务器 default_storage_engine 为 MyISAM 时事务会静默失效，utf8mb4 才能完整保存 4 字节字符
     */
    @Override
    public String generateCreateTableSql(TableInfo tableInfo) {
        return super.generateCreateTableSql(tableInfo) + " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
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
        // 插入列与参数绑定顺序保持一致（含主键），更新列排除主键
        List<ColumnInfo> columns = tableInfo.getColumns();
        List<ColumnInfo> updateColumns = tableInfo.getNonIdColumns();
        StringJoiner columnJoiner = new StringJoiner(", ");
        StringJoiner placeholderJoiner = new StringJoiner(", ");
        StringJoiner updateJoiner = new StringJoiner(", ");

        for (ColumnInfo column : columns) {
            columnJoiner.add(quoteIdentifier(column.getColumnName()));
            placeholderJoiner.add("?");
        }

        for (ColumnInfo column : updateColumns) {
            String quotedColumn = quoteIdentifier(column.getColumnName());
            updateJoiner.add(quotedColumn + " = VALUES(" + quotedColumn + ")");
        }

        // 只有主键列时退化为无实际更新的 ON DUPLICATE KEY UPDATE
        if (updateColumns.isEmpty()) {
            String quotedId = quoteIdentifier(tableInfo.getIdColumn().getColumnName());
            updateJoiner.add(quotedId + " = " + quotedId);
        }

        return "INSERT INTO " + quoteIdentifier(tableInfo.getTableName())
            + " (" + columnJoiner + ") VALUES (" + placeholderJoiner + ")"
            + " ON DUPLICATE KEY UPDATE " + updateJoiner;
    }

    @Override
    public String getBooleanType() {
        return "TINYINT(1)";
    }

}
