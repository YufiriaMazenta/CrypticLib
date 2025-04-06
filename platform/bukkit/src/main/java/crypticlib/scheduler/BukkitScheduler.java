package crypticlib.scheduler;

import crypticlib.lifecycle.AutoTask;
import crypticlib.lifecycle.BukkitLifeCycleTask;
import crypticlib.lifecycle.LifeCycle;
import crypticlib.lifecycle.TaskRule;
import crypticlib.scheduler.task.BukkitTaskWrapper;
import crypticlib.scheduler.task.TaskWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Bukkit平台的调度器
 */
@AutoTask(
    rules = @TaskRule(lifeCycle = LifeCycle.INIT)
)
public enum BukkitScheduler implements Scheduler, BukkitLifeCycleTask {

    INSTANCE;
    
    private Plugin plugin;

    @Override
    public TaskWrapper sync(@NotNull Runnable task) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTask(plugin, task));
    }

    @Override
    public TaskWrapper async(@NotNull Runnable task) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    @Override
    public TaskWrapper syncLater(@NotNull Runnable task, long delayTicks) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
    }

    @Override
    public TaskWrapper asyncLater(@NotNull Runnable task, long delayTicks) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks));
    }

    @Override
    public TaskWrapper syncTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks));
    }

    @Override
    public TaskWrapper asyncTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks));
    }

    @Override
    public TaskWrapper runOnEntity(Entity entity, Runnable task, Runnable retriedTask) {
        return sync(task);
    }

    @Override
    public TaskWrapper runOnEntityLater(Entity entity, Runnable task, Runnable retriedTask, long delayTicks) {
        return syncLater(task, delayTicks);
    }

    @Override
    public TaskWrapper runOnEntityTimer(Entity entity, Runnable task, Runnable retriedTask, long delayTicks, long periodTicks) {
        return syncTimer(task, delayTicks, periodTicks);
    }

    @Override
    public TaskWrapper runOnLocation(Location location, Runnable task) {
        return sync(task);
    }

    @Override
    public TaskWrapper runOnLocationLater(Location location, Runnable task, long delayTicks) {
        return syncLater(task, delayTicks);
    }

    @Override
    public TaskWrapper runOnLocationTimer(Location location, Runnable task, long delayTicks, long periodTicks) {
        return syncTimer(task, delayTicks, periodTicks);
    }

    @Override
    public void cancelTasks() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    @Override
    public void lifecycle(Plugin plugin, LifeCycle lifeCycle) {
        this.plugin = plugin;
    }
    
}
