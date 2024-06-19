package crypticlib.impl.scheduler;

import crypticlib.CrypticLib;
import crypticlib.internal.BukkitUtil;
import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;
import crypticlib.api.scheduler.IScheduler;
import crypticlib.impl.scheduler.task.BukkitTaskWrapper;
import crypticlib.api.scheduler.task.ITaskWrapper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Bukkit平台的调度器
 */
@PlatformSide(platform = Platform.BUKKIT)
public enum BukkitScheduler implements IScheduler {

    INSTANCE;

    @Override
    public ITaskWrapper runTask(@NotNull Runnable task) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTask(BukkitUtil.getPluginIns(), task));
    }

    @Override
    public ITaskWrapper runTaskAsync(@NotNull Runnable task) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskAsynchronously(BukkitUtil.getPluginIns(), task));
    }

    @Override
    public ITaskWrapper runTaskLater(@NotNull Runnable task, long delayTicks) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskLater(BukkitUtil.getPluginIns(), task, delayTicks));
    }

    @Override
    public ITaskWrapper runTaskLaterAsync(@NotNull Runnable task, long delayTicks) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskLaterAsynchronously(BukkitUtil.getPluginIns(), task, delayTicks));
    }

    @Override
    public ITaskWrapper runTaskTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskTimer(BukkitUtil.getPluginIns(), task, delayTicks, periodTicks));
    }

    @Override
    public ITaskWrapper runTaskTimerAsync(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskTimerAsynchronously(BukkitUtil.getPluginIns(), task, delayTicks, periodTicks));
    }

    @Override
    public void cancelTasks() {
        Bukkit.getScheduler().cancelTasks(BukkitUtil.getPluginIns());
    }

}
