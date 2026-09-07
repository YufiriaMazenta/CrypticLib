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

}
