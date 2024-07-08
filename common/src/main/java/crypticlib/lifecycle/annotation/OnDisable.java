package crypticlib.lifecycle.annotation;

import java.lang.annotation.*;

/**
 * 标记自动注销的注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnDisable { }
