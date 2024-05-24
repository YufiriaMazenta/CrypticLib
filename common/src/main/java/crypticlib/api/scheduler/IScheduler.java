package crypticlib.api.scheduler;

import crypticlib.api.scheduler.task.ITaskWrapper;
import org.jetbrains.annotations.NotNull;

/**
 * 平台调度器接口
 */
public interface IScheduler {

    /**
     * 执行一个任务
     *
     * @param task   需要执行的任务
     */
    ITaskWrapper runTask(@NotNull Runnable task);

    /**
     * 异步执行一个任务
     *
     * @param task   需要执行的任务
     */
    ITaskWrapper runTaskAsync(@NotNull Runnable task);

    /**
     * 在延迟后执行一个任务
     *
     * @param task       需要执行的任务
     * @param delayTicks 延迟的时间
     */
    ITaskWrapper runTaskLater(@NotNull Runnable task, long delayTicks);

    /**
     * 在延迟后异步执行一个任务
     *
     * @param task       需要执行的任务
     * @param delayTicks 延迟的时间，单位为tick
     */
    ITaskWrapper runTaskLaterAsync(@NotNull Runnable task, long delayTicks);

    /**
     * 在延迟后循环执行任务
     *
     * @param task        需要执行的任务
     * @param delayTicks  延迟的时间，单位为tick
     * @param periodTicks 循环执行的间隔时间，单位为tick
     */
    ITaskWrapper runTaskTimer(@NotNull Runnable task, long delayTicks, long periodTicks);

    /**
     * 在延迟后循环执行任务
     *
     * @param task        需要执行的任务
     * @param delayTicks  延迟的时间，单位为tick
     * @param periodTicks 循环执行的间隔时间，单位为tick
     */
    ITaskWrapper runTaskTimerAsync(@NotNull Runnable task, long delayTicks, long periodTicks);

    /**
     * 取消所有任务
     */
    void cancelTasks();

}
