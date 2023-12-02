package crypticlib.scheduler;

import crypticlib.scheduler.task.FoliaTaskWrapper;
import crypticlib.scheduler.task.ITaskWrapper;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Folia平台的调度器
 */
public enum FoliaScheduler implements IScheduler {

    INSTANCE;

    @Override
    public ITaskWrapper runTask(@NotNull Plugin plugin, @NotNull Runnable task) {
        return new FoliaTaskWrapper(Bukkit.getGlobalRegionScheduler().run(plugin, wrapTask(task)));
    }

    @Override
    public ITaskWrapper runTaskAsync(@NotNull Plugin plugin, @NotNull Runnable task) {
        return new FoliaTaskWrapper(Bukkit.getAsyncScheduler().runNow(plugin, wrapTask(task)));
    }

    @Override
    public ITaskWrapper runTaskLater(@NotNull Plugin plugin, @NotNull Runnable task, long delayTicks) {
        return new FoliaTaskWrapper(Bukkit.getGlobalRegionScheduler().runDelayed(plugin, wrapTask(task), delayTicks));
    }

    @Override
    public ITaskWrapper runTaskLaterAsync(@NotNull Plugin plugin, @NotNull Runnable task, long delayTicks) {
        return new FoliaTaskWrapper(Bukkit.getAsyncScheduler().runDelayed(plugin, wrapTask(task), delayTicks * 50, TimeUnit.MILLISECONDS));
    }

    @Override
    public ITaskWrapper runTaskTimer(@NotNull Plugin plugin, @NotNull Runnable task, long delayTicks, long periodTicks) {
        return new FoliaTaskWrapper(Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, wrapTask(task), delayTicks, periodTicks));
    }

    @Override
    public ITaskWrapper runTaskTimerAsync(@NotNull Plugin plugin, @NotNull Runnable task, long delayTicks, long periodTicks) {
        return new FoliaTaskWrapper(Bukkit.getAsyncScheduler().runAtFixedRate(plugin, wrapTask(task), delayTicks * 50, periodTicks * 50, TimeUnit.MILLISECONDS));
    }

    @Override
    public void cancelTasks(@NotNull Plugin plugin) {
        Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
        Bukkit.getAsyncScheduler().cancelTasks(plugin);
    }

    /**
     * 在指定实体的调度器上执行任务
     *
     * @param plugin      执行的插件
     * @param entity      执行载体
     * @param task        执行的任务
     * @param retriedTask 执行任务失败时, 重新尝试的任务
     */
    public ITaskWrapper runTaskOnEntity(Plugin plugin, Entity entity, Runnable task, Runnable retriedTask) {
        return new FoliaTaskWrapper(entity.getScheduler().run(plugin, wrapTask(task), retriedTask));
    }

    /**
     * 在指定实体的调度器上延迟执行任务
     *
     * @param plugin      执行的插件
     * @param entity      执行载体
     * @param task        执行的任务
     * @param retriedTask 执行任务失败时, 重新尝试的任务
     * @param delayTicks  延迟执行的时间
     */
    public ITaskWrapper runTaskOnEntityLater(Plugin plugin, Entity entity, Runnable task, Runnable retriedTask, long delayTicks) {
        return new FoliaTaskWrapper(entity.getScheduler().runDelayed(plugin, wrapTask(task), retriedTask, delayTicks));
    }

    /**
     * 在指定实体的调度器上延迟一段时间后重复执行任务
     *
     * @param plugin      执行的插件
     * @param entity      执行载体
     * @param task        执行的任务
     * @param retriedTask 执行任务失败时, 重新尝试的任务
     * @param delayTicks  延迟执行的时间
     * @param periodTicks 重复执行的间隔
     */
    public ITaskWrapper runTaskOnEntityTimer(Plugin plugin, Entity entity, Runnable task, Runnable retriedTask, long delayTicks, long periodTicks) {
        return new FoliaTaskWrapper(entity.getScheduler().runAtFixedRate(plugin, wrapTask(task), retriedTask, delayTicks, periodTicks));
    }

    private Consumer<ScheduledTask> wrapTask(Runnable runnable) {
        return (final ScheduledTask task) -> runnable.run();
    }

}
