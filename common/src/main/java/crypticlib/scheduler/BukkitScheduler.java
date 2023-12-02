package crypticlib.scheduler;

import crypticlib.scheduler.task.BukkitTaskWrapper;
import crypticlib.scheduler.task.ITaskWrapper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Bukkit平台的调度器
 */
public enum BukkitScheduler implements IScheduler {

    INSTANCE;

    @Override
    public ITaskWrapper runTask(@NotNull Plugin plugin, @NotNull Runnable task) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTask(plugin, task));
    }

    @Override
    public ITaskWrapper runTaskAsync(@NotNull Plugin plugin, @NotNull Runnable task) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    @Override
    public ITaskWrapper runTaskLater(@NotNull Plugin plugin, @NotNull Runnable task, long delayTicks) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
    }

    @Override
    public ITaskWrapper runTaskLaterAsync(@NotNull Plugin plugin, @NotNull Runnable task, long delayTicks) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks));
    }

    @Override
    public ITaskWrapper runTaskTimer(@NotNull Plugin plugin, @NotNull Runnable task, long delayTicks, long periodTicks) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks));
    }

    @Override
    public ITaskWrapper runTaskTimerAsync(@NotNull Plugin plugin, @NotNull Runnable task, long delayTicks, long periodTicks) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks));
    }

    @Override
    public void cancelTasks(@NotNull Plugin plugin) {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

}
