package crypticlib.scheduler;

import crypticlib.scheduler.task.ITaskWrapper;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * 平台调度器接口
 */
public interface IScheduler {

    /**
     * 执行一个任务
     * @param plugin 调用的插件
     * @param task 需要执行的任务
     */
    ITaskWrapper runTask(Plugin plugin, Runnable task);

    /**
     * 异步执行一个任务
     * @param plugin 调用的插件
     * @param task 需要执行的任务
     */
    ITaskWrapper runTaskAsync(Plugin plugin, Runnable task);

    /**
     * 在延迟后执行一个任务
     * @param plugin 调用的插件
     * @param task 需要执行的任务
     * @param delayTicks 延迟的时间
     */
    ITaskWrapper runTaskLater(Plugin plugin, Runnable task, long delayTicks);

    /**
     * 在延迟后异步执行一个任务
     * @param plugin 调用的插件
     * @param task 需要执行的任务
     * @param delayTicks 延迟的时间，单位为tick
     */
    ITaskWrapper runTaskLaterAsync(Plugin plugin, Runnable task, long delayTicks);

    /**
     * 在延迟后循环执行任务
     * @param plugin 调用的插件
     * @param task 需要执行的任务
     * @param delayTicks 延迟的时间，单位为tick
     * @param periodTicks 循环执行的间隔时间，单位为tick
     */
    ITaskWrapper runTaskTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks);

    /**
     * 在延迟后循环执行任务
     * @param plugin 调用的插件
     * @param task 需要执行的任务
     * @param delayTicks 延迟的时间，单位为tick
     * @param periodTicks 循环执行的间隔时间，单位为tick
     */
    ITaskWrapper runTaskTimerAsync(Plugin plugin, Runnable task, long delayTicks, long periodTicks);

    default void cancelTask(ITaskWrapper task) {
        if (task instanceof BukkitTask) {
            ((BukkitTask) task).cancel();
        } else if (task instanceof ScheduledTask) {
            ((ScheduledTask) task).cancel();
        } else {
            throw new AssertionError("Illegal task class " + task);
        }
    }

    void cancelTasks(Plugin plugin);
}
