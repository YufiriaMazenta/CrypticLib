package crypticlib.lifecycle.annotation;

import java.lang.annotation.*;

/**
 * 标记自动重载的注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnReload {
}
