package crypticlib.scheduler;

import crypticlib.CrypticLibPlugin;
import crypticlib.PlatformSide;
import crypticlib.lifecycle.LifecycleTaskSettings;
import crypticlib.lifecycle.LifecycleTask;
import crypticlib.lifecycle.Lifecycle;
import crypticlib.lifecycle.LifecycleRule;
import crypticlib.scheduler.task.SpigotTaskWrapper;
import crypticlib.scheduler.task.BukkitTaskWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Bukkit平台的调度器
 */
@LifecycleTaskSettings(
    rules = @LifecycleRule(lifeCycle = Lifecycle.INIT),
    platforms = PlatformSide.BUKKIT
)
public enum SpigotScheduler implements BukkitScheduler, LifecycleTask {

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
        return sync(() -> {
            if (entity == null || !entity.isValid()) {
                if (retriedTask != null) {
                    retriedTask.run();
                }
                return;
            }
            task.run();
        });
    }

    @Override
    public BukkitTaskWrapper runOnEntityLater(Entity entity, Runnable task, Runnable retriedTask, long delayTicks) {
        UUID entityId = entity.getUniqueId();
        return syncLater(() -> {
            Entity currentEntity = Bukkit.getServer().getEntity(entityId);
            if (currentEntity == null || !currentEntity.isValid()) {
                if (retriedTask != null) {
                    retriedTask.run();
                }
                return;
            }
            task.run();
        }, delayTicks);
    }

    @Override
    public BukkitTaskWrapper runOnEntityTimer(Entity entity, Runnable task, Runnable retriedTask, long delayTicks, long periodTicks) {
        // Spigot 没有 Folia 的 EntityScheduler, 这里在每个周期检查实体有效性以逼近其语义:
        // 实体失效时取消定时任务并执行 retriedTask (Folia 的 retired 回调)。
        UUID entityId = entity.getUniqueId();
        final BukkitTaskWrapper[] holder = new BukkitTaskWrapper[1];
        BukkitTaskWrapper wrapper = syncTimer(() -> {
            Entity currentEntity = Bukkit.getServer().getEntity(entityId);
            if (currentEntity == null || !currentEntity.isValid()) {
                if (holder[0] != null) {
                    holder[0].cancel();
                }
                if (retriedTask != null) {
                    retriedTask.run();
                }
                return;
            }
            task.run();
        }, delayTicks, periodTicks);
        holder[0] = wrapper;
        return wrapper;
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
    public void lifecycle(CrypticLibPlugin plugin, Lifecycle lifeCycle) {
        this.plugin = (Plugin) plugin;
    }
    
}
