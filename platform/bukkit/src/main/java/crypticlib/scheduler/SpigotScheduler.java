package crypticlib.scheduler;

import crypticlib.PlatformSide;
import crypticlib.lifecycle.LifeCycleTaskSettings;
import crypticlib.lifecycle.LifeCycleTask;
import crypticlib.lifecycle.LifeCycle;
import crypticlib.lifecycle.TaskRule;
import crypticlib.scheduler.task.SpigotTaskWrapper;
import crypticlib.scheduler.task.BukkitTaskWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Bukkit平台的调度器
 */
@LifeCycleTaskSettings(
    rules = @TaskRule(lifeCycle = LifeCycle.INIT),
    platforms = PlatformSide.BUKKIT
)
public enum SpigotScheduler implements BukkitScheduler, LifeCycleTask {

    INSTANCE;
    
    private Plugin plugin;

    @Override
    public BukkitTaskWrapper sync(@NotNull Runnable task) {
        return new SpigotTaskWrapper(Bukkit.getScheduler().runTask(plugin, task));
    }

    @Override
    public BukkitTaskWrapper async(@NotNull Runnable task) {
        return new SpigotTaskWrapper(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    @Override
    public BukkitTaskWrapper syncLater(@NotNull Runnable task, long delayTicks) {
        return new SpigotTaskWrapper(Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
    }

    @Override
    public BukkitTaskWrapper asyncLater(@NotNull Runnable task, long delayTicks) {
        return new SpigotTaskWrapper(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks));
    }

    @Override
    public BukkitTaskWrapper syncTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new SpigotTaskWrapper(Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks));
    }

    @Override
    public BukkitTaskWrapper asyncTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new SpigotTaskWrapper(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks));
    }

    @Override
    public BukkitTaskWrapper runOnEntity(Entity entity, Runnable task, Runnable retriedTask) {
        return sync(task);
    }

    @Override
    public BukkitTaskWrapper runOnEntityLater(Entity entity, Runnable task, Runnable retriedTask, long delayTicks) {
        return syncLater(task, delayTicks);
    }

    @Override
    public BukkitTaskWrapper runOnEntityTimer(Entity entity, Runnable task, Runnable retriedTask, long delayTicks, long periodTicks) {
        return syncTimer(task, delayTicks, periodTicks);
    }

    @Override
    public BukkitTaskWrapper runOnLocation(Location location, Runnable task) {
        return sync(task);
    }

    @Override
    public BukkitTaskWrapper runOnLocationLater(Location location, Runnable task, long delayTicks) {
        return syncLater(task, delayTicks);
    }

    @Override
    public BukkitTaskWrapper runOnLocationTimer(Location location, Runnable task, long delayTicks, long periodTicks) {
        return syncTimer(task, delayTicks, periodTicks);
    }

    @Override
    public void cancelTasks() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    @Override
    public void lifecycle(Object plugin, LifeCycle lifeCycle) {
        this.plugin = (Plugin) plugin;
    }
    
}
