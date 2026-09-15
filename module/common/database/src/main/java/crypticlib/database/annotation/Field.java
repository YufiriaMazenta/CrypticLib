package crypticlib.database.annotation;

import java.lang.annotation.*;

/**
 * 标记一个字段为数据库列
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Field {

    /**
     * 列名，默认使用字段名
     */
    String name() default "";

    /**
     * 是否为主键
     */
    boolean id() default false;

    /**
     * 主键是否自动生成（自增）
     */
    boolean generated() default false;

    /**
     * 是否可空
     */
    boolean nullable() default true;

    /**
     * 是否唯一
     */
    boolean unique() default false;

    /**
     * 默认值（SQL 表达式）
     */
    String defaultValue() default "";

    /**
     * 是否为外键引用
     */
    boolean foreign() default false;

    /**
     * 列类型，默认 AUTO（按 Java 类型自动识别）；显式声明时按声明的类型生成列定义
     */
    ColumnType type() default ColumnType.AUTO;

    /**
     * 列长度，默认 0（自动，等同 VARCHAR(255)）。
     * 仅对 AUTO 下的 String/枚举字段，以及显式声明的 VARCHAR 列生效，其它组合会抛异常。
     */
    int length() default 0;

    /**
     * 列类型。AUTO 以外的值会覆盖按 Java 类型自动识别的结果，由各方言翻译为对应的 SQL 类型。
     */
    enum ColumnType {

        /**
         * 按 Java 类型自动识别
         */
        AUTO,

        /**
         * 变长字符串，长度取 Field#length()，未声明时使用 255
         */
        VARCHAR,

        /**
         * 大文本（MySQL/SQLite 为 TEXT，H2 为 CLOB）
         */
        TEXT,

        TINYINT,

        SMALLINT,

        INT,

        BIGINT,

        FLOAT,

        DOUBLE,

        DECIMAL,

        BOOLEAN

    }

}
