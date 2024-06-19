package crypticlib.impl.scheduler;

import crypticlib.internal.BukkitUtil;
import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;
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
        return new FoliaTaskWrapper(Bukkit.getGlobalRegionScheduler().run(BukkitUtil.getPluginIns(), runnableToConsumer(task)));
    }

    @Override
    public ITaskWrapper runTaskAsync(@NotNull Runnable task) {
        return new FoliaTaskWrapper(Bukkit.getAsyncScheduler().runNow(BukkitUtil.getPluginIns(), runnableToConsumer(task)));
    }

    @Override
    public ITaskWrapper runTaskLater(@NotNull Runnable task, long delayTicks) {
        return new FoliaTaskWrapper(Bukkit.getGlobalRegionScheduler().runDelayed(BukkitUtil.getPluginIns(), runnableToConsumer(task), delayTicks));
    }

    @Override
    public ITaskWrapper runTaskLaterAsync(@NotNull Runnable task, long delayTicks) {
        return new FoliaTaskWrapper(Bukkit.getAsyncScheduler().runDelayed(BukkitUtil.getPluginIns(), runnableToConsumer(task), delayTicks * 50, TimeUnit.MILLISECONDS));
    }

    @Override
    public ITaskWrapper runTaskTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new FoliaTaskWrapper(Bukkit.getGlobalRegionScheduler().runAtFixedRate(BukkitUtil.getPluginIns(), runnableToConsumer(task), delayTicks, periodTicks));
    }

    @Override
    public ITaskWrapper runTaskTimerAsync(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new FoliaTaskWrapper(Bukkit.getAsyncScheduler().runAtFixedRate(BukkitUtil.getPluginIns(), runnableToConsumer(task), delayTicks * 50, periodTicks * 50, TimeUnit.MILLISECONDS));
    }

    public ITaskWrapper runTaskOnEntity(Entity entity, Runnable task, Runnable retriedTask) {
        return new FoliaTaskWrapper(entity.getScheduler().run(BukkitUtil.getPluginIns(), runnableToConsumer(task), retriedTask));
    }

    public ITaskWrapper runTaskOnEntityLater(Entity entity, Runnable task, Runnable retriedTask, long delayTicks) {
        return new FoliaTaskWrapper(entity.getScheduler().runDelayed(BukkitUtil.getPluginIns(), runnableToConsumer(task), retriedTask, delayTicks));
    }

    public ITaskWrapper runTaskOnEntityTimer(Entity entity, Runnable task, Runnable retriedTask, long delayTicks, long periodTicks) {
        return new FoliaTaskWrapper(entity.getScheduler().runAtFixedRate(BukkitUtil.getPluginIns(), runnableToConsumer(task), retriedTask, delayTicks, periodTicks));
    }

    public ITaskWrapper runTaskOnLocation(Location location, Runnable task) {
        return new FoliaTaskWrapper(Bukkit.getRegionScheduler().run(BukkitUtil.getPluginIns(), location, runnableToConsumer(task)));
    }

    public ITaskWrapper runTaskOnLocationLater(Location location, Runnable task, long delayTicks) {
        return new FoliaTaskWrapper(Bukkit.getRegionScheduler().runDelayed(BukkitUtil.getPluginIns(), location, runnableToConsumer(task), delayTicks));
    }

    public ITaskWrapper runTaskOnLocationTimer(Location location, Runnable task, long delayTicks, long periodTicks) {
        return new FoliaTaskWrapper(Bukkit.getRegionScheduler().runAtFixedRate(BukkitUtil.getPluginIns(), location, runnableToConsumer(task), delayTicks, periodTicks));
    }

    public void cancelTasks() {
        Bukkit.getGlobalRegionScheduler().cancelTasks(BukkitUtil.getPluginIns());
        Bukkit.getAsyncScheduler().cancelTasks(BukkitUtil.getPluginIns());
    }

    private Consumer<ScheduledTask> runnableToConsumer(Runnable runnable) {
        return (final ScheduledTask task) -> runnable.run();
    }

}
