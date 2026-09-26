package crypticlib.scheduler;

import crypticlib.scheduler.task.BukkitTaskWrapper;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Bukkit平台调度器接口，扩展了通用调度器接口，增加了实体/坐标调度方法
 */
public interface BukkitScheduler extends Scheduler {

    default void cancelTask(@NotNull BukkitTaskWrapper task) {
        task.cancel();
    }

    default BukkitTaskWrapper runOnEntity(@NotNull Entity entity, @NotNull Runnable task) {
        return runOnEntity(entity, task, null);
    }

    BukkitTaskWrapper runOnEntity(@NotNull Entity entity, @NotNull Runnable task, @Nullable Runnable retriedTask);

    default BukkitTaskWrapper runOnEntityLater(@NotNull Entity entity, @NotNull Runnable task, long delayTicks) {
        return runOnEntityLater(entity, task, null, delayTicks);
    }

    BukkitTaskWrapper runOnEntityLater(@NotNull Entity entity, @NotNull Runnable task, @Nullable Runnable retriedTask, long delayTicks);

    default BukkitTaskWrapper runOnEntityTimer(@NotNull Entity entity, @NotNull Runnable task, long delayTicks, long periodTicks) {
        return runOnEntityTimer(entity, task, null, delayTicks, periodTicks);
    }

    BukkitTaskWrapper runOnEntityTimer(@NotNull Entity entity, @NotNull Runnable task, @Nullable Runnable retriedTask, long delayTicks, long periodTicks);

    BukkitTaskWrapper runOnLocation(@NotNull Location location, @NotNull Runnable task);

    BukkitTaskWrapper runOnLocationLater(@NotNull Location location, @NotNull Runnable task, long delayTicks);

    BukkitTaskWrapper runOnLocationTimer(@NotNull Location location, @NotNull Runnable task, long delayTicks, long periodTicks);

}
