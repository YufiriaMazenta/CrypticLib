package crypticlib.database.query;

import crypticlib.database.dialect.DatabaseDialect;
import crypticlib.database.table.ColumnInfo;
import crypticlib.database.table.TableInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * WHERE 条件构建器
 *
 * @param <P> 父构建器类型
 */
public class Where<P> {

    private final P parentBuilder;
    private final TableInfo tableInfo;
    private final DatabaseDialect dialect;
    private final List<Condition> conditions = new ArrayList<>();

    public Where(P parentBuilder, TableInfo tableInfo, DatabaseDialect dialect) {
        this.parentBuilder = parentBuilder;
        this.tableInfo = tableInfo;
        this.dialect = dialect;
    }

    /**
     * 等于条件
     */
    public Where<P> equals(String column, Object value) {
        conditions.add(new Condition(column, "=", value));
        return this;
    }

    /**
     * 不等于条件
     */
    public Where<P> notEquals(String column, Object value) {
        conditions.add(new Condition(column, "<>", value));
        return this;
    }

    /**
     * 大于条件
     */
    public Where<P> greaterThan(String column, Object value) {
        conditions.add(new Condition(column, ">", value));
        return this;
    }

    /**
     * 大于等于条件
     */
    public Where<P> greaterThanOrEquals(String column, Object value) {
        conditions.add(new Condition(column, ">=", value));
        return this;
    }

    /**
     * 小于条件
     */
    public Where<P> lessThan(String column, Object value) {
        conditions.add(new Condition(column, "<", value));
        return this;
    }

    /**
     * 小于等于条件
     */
    public Where<P> lessThanOrEquals(String column, Object value) {
        conditions.add(new Condition(column, "<=", value));
        return this;
    }

    /**
     * LIKE 条件
     */
    public Where<P> like(String column, String value) {
        conditions.add(new Condition(column, "LIKE", value));
        return this;
    }

    /**
     * IN 条件
     */
    public Where<P> in(String column, Object... values) {
        conditions.add(new InCondition(column, values));
        return this;
    }

    /**
     * IS NULL 条件
     */
    public Where<P> isNull(String column) {
        conditions.add(new IsNullCondition(column));
        return this;
    }

    /**
     * IS NOT NULL 条件
     */
    public Where<P> isNotNull(String column) {
        conditions.add(new IsNotNullCondition(column));
        return this;
    }

    /**
     * AND 连接下一个条件
     */
    public Where<P> and() {
        if (!conditions.isEmpty()) {
            conditions.add(new Condition(null, null, null, true) {
                @Override
                String toSql() { return " AND "; }
            });
        }
        return this;
    }

    /**
     * OR 连接下一个条件
     */
    public Where<P> or() {
        if (!conditions.isEmpty()) {
            conditions.add(new Condition(null, null, null, true) {
                @Override
                String toSql() { return " OR "; }
            });
        }
        return this;
    }

    /**
     * 返回父构建器继续构建
     */
    public P done() {
        return parentBuilder;
    }

    /**
     * 构建 WHERE 子句 SQL
     */
    String buildSql() {
        if (conditions.isEmpty()) {
            return "";
        }

        StringBuilder sqlBuilder = new StringBuilder(" WHERE ");
        for (Condition condition : conditions) {
            sqlBuilder.append(condition.toSql());
        }
        return sqlBuilder.toString();
    }

    /**
     * 收集 WHERE 子句的参数值
     */
    void collectParameters(List<Object> parameters) {
        for (Condition condition : conditions) {
            if (!condition.isLogical) {
                condition.collectParameters(parameters);
            }
        }
    }

    /**
     * 条件
     */
    private class Condition {
        final String column;
        final String operator;
        final Object value;
        final boolean isLogical; // AND/OR 标记

        Condition(String column, String operator, Object value, boolean isLogical) {
            this.column = column;
            this.operator = operator;
            this.value = value;
            this.isLogical = isLogical;
        }

        Condition(String column, String operator, Object value) {
            this(column, operator, value, false);
        }

        String toSql() {
            String quotedColumn = dialect.quoteIdentifier(column);
            return quotedColumn + " " + operator + " ?";
        }

        void collectParameters(List<Object> parameters) {
            parameters.add(value);
        }
    }

    /**
     * IN 条件
     */
    private class InCondition extends Condition {
        final Object[] values;

        InCondition(String column, Object... values) {
            super(column, "IN", null);
            this.values = values;
        }

        @Override
        String toSql() {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append(dialect.quoteIdentifier(column)).append(" IN (");
            for (int i = 0; i < values.length; i++) {
                if (i > 0) sqlBuilder.append(", ");
                sqlBuilder.append("?");
            }
            sqlBuilder.append(")");
            return sqlBuilder.toString();
        }

        @Override
        void collectParameters(List<Object> parameters) {
            parameters.addAll(Arrays.asList(values));
        }
    }

    /**
     * IS NULL 条件
     */
    private class IsNullCondition extends Condition {
        IsNullCondition(String column) {
            super(column, "IS NULL", null);
        }

        @Override
        String toSql() {
            return dialect.quoteIdentifier(column) + " IS NULL";
        }

        @Override
        void collectParameters(List<Object> parameters) {
            // 无参数
        }
    }

    /**
     * IS NOT NULL 条件
     */
    private class IsNotNullCondition extends Condition {
        IsNotNullCondition(String column) {
            super(column, "IS NOT NULL", null);
        }

        @Override
        String toSql() {
            return dialect.quoteIdentifier(column) + " IS NOT NULL";
        }

        @Override
        void collectParameters(List<Object> parameters) {
            // 无参数
        }
    }

}
