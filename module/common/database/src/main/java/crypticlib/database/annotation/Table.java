package crypticlib.database.annotation;

import java.lang.annotation.*;

/**
 * 标记一个类为数据库表实体
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

    /**
     * 表名，默认使用类名
     */
    String name() default "";

}
