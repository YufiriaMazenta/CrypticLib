package crypticlib.listener;

import java.lang.annotation.*;

/**
 * 用于插件加载时自动注册监听器的注解，拥有此注解的监听器将会被自动注册
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventListener {

    /**
     * 新建实例时是否无视ClassNotFoundException和NoClassDefFoundError
     * 若为true,遇到上述异常将不会打印报错
     * 若为false,将会打印报错堆栈信息
     */
    boolean ignoreClassNotFound() default true;

}
