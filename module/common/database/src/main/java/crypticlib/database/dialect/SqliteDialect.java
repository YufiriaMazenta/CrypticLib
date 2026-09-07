package crypticlib.database.dialect;

import crypticlib.database.table.ColumnInfo;
import crypticlib.database.table.TableInfo;

import java.util.List;

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

    /**
     * SQLite 的自增主键必须写成 INTEGER PRIMARY KEY AUTOINCREMENT，不能分开
     */
    @Override
    public String generateCreateTableSql(TableInfo tableInfo) {
        StringBuilder sqlBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sqlBuilder.append(quoteIdentifier(tableInfo.getTableName()));
        sqlBuilder.append(" (");

        ColumnInfo idColumn = tableInfo.getIdColumn();
        List<ColumnInfo> columns = tableInfo.getColumns();
        boolean first = true;

        for (ColumnInfo column : columns) {
            if (!first) sqlBuilder.append(", ");
            first = false;

            if (column.isId() && column.isGenerated()) {
                // SQLite 自增主键：INTEGER PRIMARY KEY AUTOINCREMENT
                sqlBuilder.append(quoteIdentifier(column.getColumnName()));
                sqlBuilder.append(" INTEGER PRIMARY KEY AUTOINCREMENT");
            } else {
                sqlBuilder.append(generateColumnDefinition(column));
            }
        }

        // 如果有非自增的主键，添加 PRIMARY KEY 约束
        if (idColumn != null && !idColumn.isGenerated()) {
            sqlBuilder.append(", PRIMARY KEY (");
            sqlBuilder.append(quoteIdentifier(idColumn.getColumnName()));
            sqlBuilder.append(")");
        }

        sqlBuilder.append(")");
        return sqlBuilder.toString();
    }

    @Override
    public String generateColumnDefinition(ColumnInfo columnInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(quoteIdentifier(columnInfo.getColumnName()));
        sb.append(" ");
        sb.append(mapJavaType(columnInfo.getJavaType()));

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
