package crypticlib.database.dialect;

import crypticlib.database.table.ColumnInfo;
import crypticlib.database.table.TableInfo;

import java.util.List;

/**
 * 数据库方言接口，用于适配不同数据库的 SQL 语法差异
 */
public interface DatabaseDialect {

    /**
     * 引用标识符（表名、列名）
     */
    String quoteIdentifier(String identifier);

    /**
     * 获取自增列定义 SQL
     */
    String getAutoIncrementSql();

    /**
     * 追加分页语句
     */
    String appendLimitOffset(String sql, long limit, long offset);

    /**
     * 获取布尔类型的 SQL 定义
     */
    String getBooleanType();

    /**
     * 映射 Java 类型到 SQL 类型
     */
    String mapJavaType(Class<?> javaType);

    /**
     * 生成建表 SQL
     */
    String generateCreateTableSql(TableInfo tableInfo);

    /**
     * 生成插入 SQL
     */
    String generateInsertSql(TableInfo tableInfo);

    /**
     * 生成更新 SQL
     */
    String generateUpdateSql(TableInfo tableInfo);

    /**
     * 生成删除 SQL
     */
    String generateDeleteSql(TableInfo tableInfo);

    /**
     * 生成根据 ID 查询 SQL
     */
    String generateQueryByIdSql(TableInfo tableInfo);

    /**
     * 生成查询全部 SQL
     */
    String generateQueryAllSql(TableInfo tableInfo);

    /**
     * 生成替换（INSERT OR REPLACE）SQL
     */
    String generateReplaceSql(TableInfo tableInfo);

    /**
     * 获取列定义 SQL 片段
     */
    String generateColumnDefinition(ColumnInfo columnInfo);

    /**
     * 转换查询参数类型
     * 将 Java 对象转换为 JDBC 兼容的类型
     *
     * @param value 原始参数值
     * @return 转换后的参数值
     */
    Object convertParameter(Object value);

}
