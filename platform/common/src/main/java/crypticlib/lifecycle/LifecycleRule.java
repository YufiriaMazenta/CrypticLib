package crypticlib.lifecycle;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LifecycleRule {

    Lifecycle lifeCycle();

    /**
     * 生命周期任务的优先级,同生命周期的任务会按照优先级从小到大运行
     * @return 生命周期任务的优先级
     */
    int priority() default 0;

    /**
     * 这个生命周期任务是否在异步线程运行
     * @return 任务是否在异步进行
     */
    boolean isAsync() default false;

}
