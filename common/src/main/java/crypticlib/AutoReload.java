package crypticlib;

import java.lang.annotation.*;

/**
 * 标记自动重载的注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoReload {
}
