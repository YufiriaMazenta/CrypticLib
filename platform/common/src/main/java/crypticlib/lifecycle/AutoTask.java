package crypticlib.lifecycle;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoTask {

    TaskRule[] rules();

    /**
     * 新建实例时是否无视ClassNotFoundException和NoClassDefFoundError
     * 若为true,遇到上述异常将不会打印报错
     * 若为false,将会打印报错堆栈信息
     */
    boolean ignoreClassNotFound() default true;

}
