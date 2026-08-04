package crypticlib.lifecycle;

import crypticlib.CrypticLibPlugin;

/**
 * 用于在指定的生命周期执行任务
 * 需要搭配{@link LifecycleTaskSettings}注解使用
 */
public interface LifecycleTask {

    void lifecycle(CrypticLibPlugin plugin, Lifecycle lifeCycle);

}
