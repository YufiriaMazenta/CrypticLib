package crypticlib.lifecycle;

/**
 * 用于在指定的生命周期执行任务
 * 需要搭配{@link LifeCycleTaskSettings}注解使用
 */
public interface LifeCycleTask {

    void lifecycle(Object plugin, LifeCycle lifeCycle);

}
