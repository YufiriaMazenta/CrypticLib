package crypticlib.database.query;

import crypticlib.database.dialect.DatabaseDialect;
import crypticlib.database.table.ColumnInfo;
import crypticlib.database.table.TableInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * WHERE 条件构建器
 */
public class Where {

    private final Object parentBuilder;
    private final TableInfo tableInfo;
    private final DatabaseDialect dialect;
    private final List<Condition> conditions = new ArrayList<>();

    public Where(Object parentBuilder, TableInfo tableInfo, DatabaseDialect dialect) {
        this.parentBuilder = parentBuilder;
        this.tableInfo = tableInfo;
        this.dialect = dialect;
    }

    /**
     * 等于条件
     */
    public Where eq(String column, Object value) {
        conditions.add(new Condition(column, "=", value));
        return this;
    }

    /**
     * 不等于条件
     */
    public Where ne(String column, Object value) {
        conditions.add(new Condition(column, "<>", value));
        return this;
    }

    /**
     * 大于条件
     */
    public Where gt(String column, Object value) {
        conditions.add(new Condition(column, ">", value));
        return this;
    }

    /**
     * 大于等于条件
     */
    public Where gte(String column, Object value) {
        conditions.add(new Condition(column, ">=", value));
        return this;
    }

    /**
     * 小于条件
     */
    public Where lt(String column, Object value) {
        conditions.add(new Condition(column, "<", value));
        return this;
    }

    /**
     * 小于等于条件
     */
    public Where lte(String column, Object value) {
        conditions.add(new Condition(column, "<=", value));
        return this;
    }

    /**
     * LIKE 条件
     */
    public Where like(String column, String value) {
        conditions.add(new Condition(column, "LIKE", value));
        return this;
    }

    /**
     * IN 条件
     */
    public Where in(String column, Object... values) {
        conditions.add(new InCondition(column, values));
        return this;
    }

    /**
     * IS NULL 条件
     */
    public Where isNull(String column) {
        conditions.add(new IsNullCondition(column));
        return this;
    }

    /**
     * IS NOT NULL 条件
     */
    public Where isNotNull(String column) {
        conditions.add(new IsNotNullCondition(column));
        return this;
    }

    /**
     * AND 连接下一个条件
     */
    public Where and() {
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
    public Where or() {
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
    @SuppressWarnings("unchecked")
    public <P> P done() {
        return (P) parentBuilder;
    }

    /**
     * 构建 WHERE 子句 SQL
     */
    String buildSql() {
        if (conditions.isEmpty()) {
            return "";
        }

        StringBuilder sqlBuilder = new StringBuilder(" WHERE ");
        for (int i = 0; i < conditions.size(); i++) {
            Condition condition = conditions.get(i);
            sqlBuilder.append(condition.toSql());
        }
        return sqlBuilder.toString();
    }

    /**
     * 收集 WHERE 子句的参数值
     */
    List<Object> collectParameters(List<Object> parameters) {
        for (Condition condition : conditions) {
            if (!condition.isLogical) {
                condition.collectParameters(parameters);
            }
        }
        return parameters;
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
            for (Object value : values) {
                parameters.add(value);
            }
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
