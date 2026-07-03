package crypticlib.lifecycle;

import crypticlib.PlatformSide;

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
     * 指定该任务在哪些平台执行
     * 默认在所有平台都会运行
     * @return
     */
    PlatformSide[] platforms() default { PlatformSide.BUKKIT, PlatformSide.BUNGEE, PlatformSide.VELOCITY };

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
