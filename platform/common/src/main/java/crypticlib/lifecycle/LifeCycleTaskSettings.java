package crypticlib.lifecycle;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LifeCycleTaskSettings {

    /**
     * 用于标记该生命周期任务的执行规则
     * @return
     */
    TaskRule[] rules();

    /**
     * 应该忽视掉的异常
     * 此项中的异常发生时，将静默处理
     */
    Class<? extends Throwable>[] ignoreExceptions() default {ClassNotFoundException.class, NoClassDefFoundError.class};

    /**
     * 应该打印出来的异常
     * 此项中的异常发生时，将打印该异常，并且不抛出错误
     */
    Class<? extends Throwable>[] printExceptions() default {};

}
