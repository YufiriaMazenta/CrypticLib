package crypticlib.impl.scheduler;

import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;
import crypticlib.platform.bukkit.BukkitPlugin;
import crypticlib.api.scheduler.IScheduler;
import crypticlib.impl.scheduler.task.FoliaTaskWrapper;
import crypticlib.api.scheduler.task.ITaskWrapper;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Folia平台的调度器
 */
@PlatformSide(platform = Platform.BUKKIT)
public enum FoliaScheduler implements IScheduler {

    INSTANCE;

    @Override
    public ITaskWrapper runTask(@NotNull Runnable task) {
        return new FoliaTaskWrapper(Bukkit.getGlobalRegionScheduler().run(BukkitPlugin.INSTANCE, runnableToConsumer(task)));
    }

    @Override
    public ITaskWrapper runTaskAsync(@NotNull Runnable task) {
        return new FoliaTaskWrapper(Bukkit.getAsyncScheduler().runNow(BukkitPlugin.INSTANCE, runnableToConsumer(task)));
    }

    @Override
    public ITaskWrapper runTaskLater(@NotNull Runnable task, long delayTicks) {
        return new FoliaTaskWrapper(Bukkit.getGlobalRegionScheduler().runDelayed(BukkitPlugin.INSTANCE, runnableToConsumer(task), delayTicks));
    }

    @Override
    public ITaskWrapper runTaskLaterAsync(@NotNull Runnable task, long delayTicks) {
        return new FoliaTaskWrapper(Bukkit.getAsyncScheduler().runDelayed(BukkitPlugin.INSTANCE, runnableToConsumer(task), delayTicks * 50, TimeUnit.MILLISECONDS));
    }

    @Override
    public ITaskWrapper runTaskTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new FoliaTaskWrapper(Bukkit.getGlobalRegionScheduler().runAtFixedRate(BukkitPlugin.INSTANCE, runnableToConsumer(task), delayTicks, periodTicks));
    }

    @Override
    public ITaskWrapper runTaskTimerAsync(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new FoliaTaskWrapper(Bukkit.getAsyncScheduler().runAtFixedRate(BukkitPlugin.INSTANCE, runnableToConsumer(task), delayTicks * 50, periodTicks * 50, TimeUnit.MILLISECONDS));
    }

    public ITaskWrapper runTaskOnEntity(Entity entity, Runnable task, Runnable retriedTask) {
        return new FoliaTaskWrapper(entity.getScheduler().run(BukkitPlugin.INSTANCE, runnableToConsumer(task), retriedTask));
    }

    public ITaskWrapper runTaskOnEntityLater(Entity entity, Runnable task, Runnable retriedTask, long delayTicks) {
        return new FoliaTaskWrapper(entity.getScheduler().runDelayed(BukkitPlugin.INSTANCE, runnableToConsumer(task), retriedTask, delayTicks));
    }

    public ITaskWrapper runTaskOnEntityTimer(Entity entity, Runnable task, Runnable retriedTask, long delayTicks, long periodTicks) {
        return new FoliaTaskWrapper(entity.getScheduler().runAtFixedRate(BukkitPlugin.INSTANCE, runnableToConsumer(task), retriedTask, delayTicks, periodTicks));
    }

    public ITaskWrapper runTaskOnLocation(Location location, Runnable task) {
        return new FoliaTaskWrapper(Bukkit.getRegionScheduler().run(BukkitPlugin.INSTANCE, location, runnableToConsumer(task)));
    }

    public ITaskWrapper runTaskOnLocationLater(Location location, Runnable task, long delayTicks) {
        return new FoliaTaskWrapper(Bukkit.getRegionScheduler().runDelayed(BukkitPlugin.INSTANCE, location, runnableToConsumer(task), delayTicks));
    }

    public ITaskWrapper runTaskOnLocationTimer(Location location, Runnable task, long delayTicks, long periodTicks) {
        return new FoliaTaskWrapper(Bukkit.getRegionScheduler().runAtFixedRate(BukkitPlugin.INSTANCE, location, runnableToConsumer(task), delayTicks, periodTicks));
    }

    public void cancelTasks() {
        Bukkit.getGlobalRegionScheduler().cancelTasks(BukkitPlugin.INSTANCE);
        Bukkit.getAsyncScheduler().cancelTasks(BukkitPlugin.INSTANCE);
    }

    private Consumer<ScheduledTask> runnableToConsumer(Runnable runnable) {
        return (final ScheduledTask task) -> runnable.run();
    }

}
