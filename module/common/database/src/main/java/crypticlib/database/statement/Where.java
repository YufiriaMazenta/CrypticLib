package crypticlib.database.statement;

import crypticlib.database.dialect.AbstractDialect;
import crypticlib.database.dialect.DatabaseDialect;
import crypticlib.database.table.ColumnInfo;
import crypticlib.database.table.TableInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * WHERE 条件构建器
 * <p>
 * 列名会与实体的列元数据校验，值会按列的 Java 类型转换；条件值不允许为 null（请改用 isNull/isNotNull）。
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
        return addComparison(column, "=", value);
    }

    /**
     * 不等于条件
     */
    public Where<P> notEquals(String column, Object value) {
        return addComparison(column, "<>", value);
    }

    /**
     * 大于条件
     */
    public Where<P> greaterThan(String column, Object value) {
        return addComparison(column, ">", value);
    }

    /**
     * 大于等于条件
     */
    public Where<P> greaterThanOrEquals(String column, Object value) {
        return addComparison(column, ">=", value);
    }

    /**
     * 小于条件
     */
    public Where<P> lessThan(String column, Object value) {
        return addComparison(column, "<", value);
    }

    /**
     * 小于等于条件
     */
    public Where<P> lessThanOrEquals(String column, Object value) {
        return addComparison(column, "<=", value);
    }

    /**
     * LIKE 条件
     */
    public Where<P> like(String column, String value) {
        return addComparison(column, "LIKE", value);
    }

    /**
     * IN 条件，至少需要一个值
     */
    public Where<P> in(String column, Object... values) {
        ColumnInfo columnInfo = tableInfo.requireColumn(column);
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("in() of column \"" + column + "\" requires at least one value");
        }
        Object[] coercedValues = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                throw new IllegalArgumentException("Values passed to in() of column \"" + column
                    + "\" must not be null");
            }
            coercedValues[i] = AbstractDialect.coerceValue(values[i], columnInfo.getJavaType());
        }
        conditions.add(new InCondition(column, coercedValues));
        return this;
    }

    /**
     * IS NULL 条件
     */
    public Where<P> isNull(String column) {
        tableInfo.requireColumn(column);
        conditions.add(new IsNullCondition(column));
        return this;
    }

    /**
     * IS NOT NULL 条件
     */
    public Where<P> isNotNull(String column) {
        tableInfo.requireColumn(column);
        conditions.add(new IsNotNullCondition(column));
        return this;
    }

    /**
     * AND 连接下一个条件，必须紧跟在一个条件之后
     */
    public Where<P> and() {
        return addLogical("AND");
    }

    /**
     * OR 连接下一个条件，必须紧跟在一个条件之后
     */
    public Where<P> or() {
        return addLogical("OR");
    }

    /**
     * 返回父构建器继续构建
     */
    public P done() {
        return parentBuilder;
    }

    /**
     * 是否还没有任何条件
     */
    boolean isEmpty() {
        return conditions.isEmpty();
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
     * 校验列名并把值转成列对应的 Java 类型后加入条件
     */
    private Where<P> addComparison(String column, String operator, Object value) {
        ColumnInfo columnInfo = tableInfo.requireColumn(column);
        if (value == null) {
            throw new IllegalArgumentException("Value of the condition on column \"" + column + "\" must not be null, "
                + "a null value can never match: use isNull(\"" + column + "\") / isNotNull(\"" + column + "\") instead");
        }
        conditions.add(new Condition(column, operator, AbstractDialect.coerceValue(value, columnInfo.getJavaType())));
        return this;
    }

    /**
     * 加入 AND / OR 连接符，悬空或重复的连接符在构建期直接报错
     */
    private Where<P> addLogical(String operator) {
        if (conditions.isEmpty()) {
            throw new IllegalStateException(operator + " must follow a condition");
        }
        if (conditions.get(conditions.size() - 1).isLogical) {
            throw new IllegalStateException("Duplicate " + operator + " separator in the where clause");
        }
        conditions.add(new LogicalCondition(operator));
        return this;
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
     * AND / OR 连接符
     */
    private class LogicalCondition extends Condition {
        private final String logicalOperator;

        LogicalCondition(String logicalOperator) {
            super(null, null, null, true);
            this.logicalOperator = logicalOperator;
        }

        @Override
        String toSql() {
            return " " + logicalOperator + " ";
        }
    }

    /**
     * IN 条件
     */
    private class InCondition extends Condition {
        final Object[] values;

        InCondition(String column, Object[] values) {
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