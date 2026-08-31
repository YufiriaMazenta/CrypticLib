package crypticlib.lifecycle;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LifecycleSchedule {

    LifecyclePhase phase();

    /**
     * 生命周期任务的优先级,同生命周期的任务会按照优先级从小到大运行
     * @return 生命周期任务的优先级
     */
    int priority() default 0;

    /**
     * 这个生命周期任务是否在异步线程运行
     * 异步生命周期任务只能保证同生命周期阶段的任务按照顺序执行, 其他异步生命周期任务的顺序性无法保证
     * 可能出现例如ENABLE阶段的异步生命周期任务比LOAD阶段更早完成这样的情况
     * @return 任务是否在异步进行
     */
    boolean isAsync() default false;

}
