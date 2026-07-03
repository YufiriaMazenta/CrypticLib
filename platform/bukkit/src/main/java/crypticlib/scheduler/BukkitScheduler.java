package crypticlib.scheduler;

import crypticlib.scheduler.task.BukkitTaskWrapper;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Bukkit平台调度器接口，扩展了通用调度器接口，增加了实体/坐标调度方法
 */
public interface BukkitScheduler extends Scheduler {

    default void cancelTask(@NotNull BukkitTaskWrapper task) {
        task.cancel();
    }

    BukkitTaskWrapper runOnEntity(Entity entity, Runnable task, Runnable retriedTask);

    BukkitTaskWrapper runOnEntityLater(Entity entity, Runnable task, Runnable retriedTask, long delayTicks);

    BukkitTaskWrapper runOnEntityTimer(Entity entity, Runnable task, Runnable retriedTask, long delayTicks, long periodTicks);

    BukkitTaskWrapper runOnLocation(Location location, Runnable task);

    BukkitTaskWrapper runOnLocationLater(Location location, Runnable task, long delayTicks);

    BukkitTaskWrapper runOnLocationTimer(Location location, Runnable task, long delayTicks, long periodTicks);

}
