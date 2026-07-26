package crypticlib.scheduler;

import crypticlib.PlatformSide;
import crypticlib.lifecycle.LifeCycleTaskSettings;
import crypticlib.lifecycle.LifeCycleTask;
import crypticlib.lifecycle.LifeCycle;
import crypticlib.lifecycle.TaskRule;
import crypticlib.scheduler.task.FoliaTaskWrapper;
import crypticlib.scheduler.task.BukkitTaskWrapper;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Folia平台的调度器
 */
@LifeCycleTaskSettings(
    rules = @TaskRule(lifeCycle = LifeCycle.INIT),
    platforms = PlatformSide.BUKKIT
)
public enum FoliaScheduler implements BukkitScheduler, LifeCycleTask {

    INSTANCE;

    private Plugin plugin;
    /**
     * 记录本调度器创建的 location/entity 定时任务, 供 cancelTasks 手动取消:
     * Folia 的 RegionScheduler/EntityScheduler 不提供按插件取消的 API。
     */
    private final Set<BukkitTaskWrapper> regionEntityTasks = ConcurrentHashMap.newKeySet();

    @Override
    public BukkitTaskWrapper sync(@NotNull Runnable task) {
        return new FoliaTaskWrapper(Bukkit.getGlobalRegionScheduler().run(plugin, runnableToConsumer(task)));
    }

    @Override
    public BukkitTaskWrapper async(@NotNull Runnable task) {
        return new FoliaTaskWrapper(Bukkit.getAsyncScheduler().runNow(plugin, runnableToConsumer(task)));
    }

    @Override
    public BukkitTaskWrapper syncLater(@NotNull Runnable task, long delayTicks) {
        return new FoliaTaskWrapper(Bukkit.getGlobalRegionScheduler().runDelayed(plugin, runnableToConsumer(task), toSafeTick(delayTicks)));
    }

    @Override
    public BukkitTaskWrapper asyncLater(@NotNull Runnable task, long delayTicks) {
        return new FoliaTaskWrapper(Bukkit.getAsyncScheduler().runDelayed(plugin, runnableToConsumer(task), toSafeTick(delayTicks) * 50, TimeUnit.MILLISECONDS));
    }

    @Override
    public BukkitTaskWrapper syncTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new FoliaTaskWrapper(Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, runnableToConsumer(task), toSafeTick(delayTicks), toSafeTick(periodTicks)));
    }

    @Override
    public BukkitTaskWrapper asyncTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new FoliaTaskWrapper(Bukkit.getAsyncScheduler().runAtFixedRate(plugin, runnableToConsumer(task), toSafeTick(delayTicks) * 50, toSafeTick(periodTicks) * 50, TimeUnit.MILLISECONDS));
    }

    @Override
    public BukkitTaskWrapper runOnEntity(Entity entity, Runnable task, Runnable retriedTask) {
        return wrapEntityTask(entity.getScheduler().run(plugin, runnableToConsumer(task), retriedTask));
    }


    @Override
    public BukkitTaskWrapper runOnEntityLater(Entity entity, Runnable task, Runnable retriedTask, long delayTicks) {
        return wrapEntityTask(entity.getScheduler().runDelayed(plugin, runnableToConsumer(task), retriedTask, toSafeTick(delayTicks)));
    }

    @Override
    public BukkitTaskWrapper runOnEntityTimer(Entity entity, Runnable task, Runnable retriedTask, long delayTicks, long periodTicks) {
        return trackRepeatingTask(wrapEntityTask(entity.getScheduler().runAtFixedRate(plugin, runnableToConsumer(task), retriedTask, toSafeTick(delayTicks), toSafeTick(periodTicks))));
    }

    @Override
    public BukkitTaskWrapper runOnLocation(Location location, Runnable task) {
        return new FoliaTaskWrapper(Bukkit.getRegionScheduler().run(plugin, location, runnableToConsumer(task)));
    }

    @Override
    public BukkitTaskWrapper runOnLocationLater(Location location, Runnable task, long delayTicks) {
        return new FoliaTaskWrapper(Bukkit.getRegionScheduler().runDelayed(plugin, location, runnableToConsumer(task), toSafeTick(delayTicks)));
    }

    @Override
    public BukkitTaskWrapper runOnLocationTimer(Location location, Runnable task, long delayTicks, long periodTicks) {
        return trackRepeatingTask(new FoliaTaskWrapper(Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, runnableToConsumer(task), toSafeTick(delayTicks), toSafeTick(periodTicks))));
    }

    @Override
    public void cancelTasks() {
        Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
        Bukkit.getAsyncScheduler().cancelTasks(plugin);
        // Folia 的 RegionScheduler/EntityScheduler 不提供按插件取消的 API,
        // 手动取消本调度器记录的 location/entity 定时任务, 逼近 Spigot cancelTasks 的语义。
        for (BukkitTaskWrapper wrapper : regionEntityTasks) {
            wrapper.cancel();
        }
        regionEntityTasks.clear();
    }

    /**
     * 包装 EntityScheduler 的返回值。Folia 的 EntityScheduler#run/runDelayed/runAtFixedRate
     * 在实体已被移除(死亡、卸载等)时返回 null, 直接包成 FoliaTaskWrapper 会在后续
     * cancel()/isCancelled() 处抛 NPE。此处对 null 返回一个已取消语义的空实现。
     */
    private BukkitTaskWrapper wrapEntityTask(ScheduledTask scheduledTask) {
        if (scheduledTask == null) {
            return new CancelledTaskWrapper(plugin);
        }
        return new FoliaTaskWrapper(scheduledTask);
    }

    private BukkitTaskWrapper trackRepeatingTask(BukkitTaskWrapper wrapper) {
        regionEntityTasks.add(wrapper);
        return wrapper;
    }

    /**
     * 用于实体已被移除时 EntityScheduler 返回 null 的场景: cancel() 为 no-op,
     * isCancelled() 恒为 true, 表示该任务从未真正被调度。
     */
    private static final class CancelledTaskWrapper implements BukkitTaskWrapper {

        private final Plugin plugin;

        private CancelledTaskWrapper(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void cancel() {
        }

        @Override
        public boolean isCancelled() {
            return true;
        }

        @Override
        public @NotNull Plugin owner() {
            return plugin;
        }

        @Override
        public Integer taskId() {
            throw new UnsupportedOperationException("Cancelled folia task can not get task id");
        }

        @Override
        public @NotNull Object platformTask() {
            throw new UnsupportedOperationException("Cancelled folia task has no platform task");
        }
    }

    private Consumer<ScheduledTask> runnableToConsumer(Runnable runnable) {
        return (final ScheduledTask task) -> runnable.run();
    }

    private long toSafeTick(long originTick) {
        return originTick > 0 ? originTick : 1;
    }

    @Override
    public void lifecycle(Object plugin, LifeCycle lifeCycle) {
        this.plugin = (Plugin) plugin;
    }
    
}
