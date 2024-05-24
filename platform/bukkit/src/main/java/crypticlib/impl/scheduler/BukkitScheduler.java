package crypticlib.impl.scheduler;

import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;
import crypticlib.platform.bukkit.BukkitPlugin;
import crypticlib.api.scheduler.IScheduler;
import crypticlib.impl.scheduler.task.BukkitTaskWrapper;
import crypticlib.api.scheduler.task.ITaskWrapper;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

/**
 * Bukkit平台的调度器
 */
@PlatformSide(platform = Platform.BUKKIT)
public enum BukkitScheduler implements IScheduler {

    INSTANCE;

    @Override
    public ITaskWrapper runTask(@NotNull Runnable task) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTask(BukkitPlugin.INSTANCE, task));
    }

    @Override
    public ITaskWrapper runTaskAsync(@NotNull Runnable task) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskAsynchronously(BukkitPlugin.INSTANCE, task));
    }

    @Override
    public ITaskWrapper runTaskLater(@NotNull Runnable task, long delayTicks) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskLater(BukkitPlugin.INSTANCE, task, delayTicks));
    }

    @Override
    public ITaskWrapper runTaskLaterAsync(@NotNull Runnable task, long delayTicks) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskLaterAsynchronously(BukkitPlugin.INSTANCE, task, delayTicks));
    }

    @Override
    public ITaskWrapper runTaskTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskTimer(BukkitPlugin.INSTANCE, task, delayTicks, periodTicks));
    }

    @Override
    public ITaskWrapper runTaskTimerAsync(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskTimerAsynchronously(BukkitPlugin.INSTANCE, task, delayTicks, periodTicks));
    }

    @Override
    public void cancelTasks() {
        Bukkit.getScheduler().cancelTasks(BukkitPlugin.INSTANCE);
    }

}
