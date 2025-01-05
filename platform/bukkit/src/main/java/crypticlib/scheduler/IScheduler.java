package crypticlib.scheduler;

import crypticlib.scheduler.task.ITaskWrapper;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;
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
    ITaskWrapper sync(@NotNull Runnable task);

    /**
     * 异步执行一个任务
     *
     * @param task   需要执行的任务
     */
    ITaskWrapper async(@NotNull Runnable task);

    /**
     * 在延迟后执行一个任务
     *
     * @param task       需要执行的任务
     * @param delayTicks 延迟的时间
     */
    ITaskWrapper syncLater(@NotNull Runnable task, long delayTicks);

    /**
     * 在延迟后异步执行一个任务
     *
     * @param task       需要执行的任务
     * @param delayTicks 延迟的时间，单位为tick
     */
    ITaskWrapper asyncLater(@NotNull Runnable task, long delayTicks);

    /**
     * 在延迟后循环执行任务
     *
     * @param task        需要执行的任务
     * @param delayTicks  延迟的时间，单位为tick
     * @param periodTicks 循环执行的间隔时间，单位为tick
     */
    ITaskWrapper syncTimer(@NotNull Runnable task, long delayTicks, long periodTicks);

    /**
     * 在延迟后循环执行任务
     *
     * @param task        需要执行的任务
     * @param delayTicks  延迟的时间，单位为tick
     * @param periodTicks 循环执行的间隔时间，单位为tick
     */
    ITaskWrapper asyncTimer(@NotNull Runnable task, long delayTicks, long periodTicks);

    /**
     * 取消某个任务
     *
     * @param task 需要取消的任务的包装
     */
    default void cancelTask(@NotNull ITaskWrapper task) {
        if (task instanceof BukkitTask) {
            ((BukkitTask) task).cancel();
        } else if (task instanceof ScheduledTask) {
            ((ScheduledTask) task).cancel();
        } else {
            throw new AssertionError("Illegal task class " + task);
        }
    }

    void cancelTasks();

    /**
     * 在指定实体的调度器上执行任务
     * 当平台为非Folia时，效果等同于runTask
     *
     * @param entity      执行载体
     * @param task        执行的任务
     * @param retriedTask 执行任务失败时, 重新尝试的任务
     */
    ITaskWrapper runOnEntity(Entity entity, Runnable task, Runnable retriedTask);

    /**
     * 在指定实体的调度器上延迟执行任务
     * 当平台为非Folia时，效果等同于runTaskLater
     *
     * @param entity      执行载体
     * @param task        执行的任务
     * @param retriedTask 执行任务失败时, 重新尝试的任务
     * @param delayTicks  延迟执行的时间
     */
    ITaskWrapper runOnEntityLater(Entity entity, Runnable task, Runnable retriedTask, long delayTicks);

    /**
     * 在指定实体的调度器上延迟一段时间后重复执行任务
     * 当平台为非Folia时，效果等同于runTaskTimer
     *
     * @param entity      执行载体
     * @param task        执行的任务
     * @param retriedTask 执行任务失败时, 重新尝试的任务
     * @param delayTicks  延迟执行的时间
     * @param periodTicks 重复执行的间隔
     */
    ITaskWrapper runOnEntityTimer(Entity entity, Runnable task, Runnable retriedTask, long delayTicks, long periodTicks);

    /**
     * 在指定坐标的调度器上执行任务
     * 当平台为非Folia时，效果等同于runTask
     *
     * @param location    执行载体
     * @param task        执行的任务
     */
    ITaskWrapper runOnLocation(Location location, Runnable task);


    /**
     * 在指定实体的调度器上延迟执行任务
     * 当平台为非Folia时，效果等同于runTaskLater
     *
     * @param location    执行载体
     * @param task        执行的任务
     * @param delayTicks  延迟执行的时间
     */
    ITaskWrapper runOnLocationLater(Location location, Runnable task, long delayTicks);

    /**
     * 在指定实体的调度器上延迟一段时间后重复执行任务
     * 当平台为非Folia时，效果等同于runTaskTimer
     *
     * @param location    执行载体
     * @param task        执行的任务
     * @param delayTicks  延迟执行的时间
     * @param periodTicks 重复执行的间隔
     */
    ITaskWrapper runOnLocationTimer(Location location, Runnable task, long delayTicks, long periodTicks);

}
