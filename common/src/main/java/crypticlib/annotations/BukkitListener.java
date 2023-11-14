package crypticlib.annotations;

import java.lang.annotation.*;

/**
 * 用于插件加载时自动注册监听器的注解，拥有此注解的监听器将会被自动注册
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BukkitListener {

    /**
     * 是否注册此监听器
     * @return 是否注册此监听器
     */
    boolean reg() default true;

}
