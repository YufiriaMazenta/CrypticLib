package crypticlib.platformadapter;

import crypticlib.scheduler.BukkitScheduler;
import crypticlib.scheduler.Scheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Bukkit平台的一些方法集成
 */
public enum BukkitPlatformAdapter implements PlatformAdapter {

    INSTANCE;

    @Override
    @NotNull
    public PlatformAdapter.PlatformType type() {
        return PlatformType.BUKKIT;
    }

    @Override
    @NotNull
    public Scheduler scheduler() {
        return BukkitScheduler.INSTANCE;
    }

    @Override
    public Future<Boolean> teleportEntity(@NotNull Entity entity, @NotNull Location location, PlayerTeleportEvent.@NotNull TeleportCause cause) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        future.complete(entity.teleport(location, cause));
        return future;
    }

    @Override
    public Future<Boolean> teleportEntity(@NotNull Entity entity, @NotNull Location location) {
        return teleportEntity(entity, location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @Override
    public boolean isBukkit() {
        return true;
    }

    @Override
    public boolean isPaper() {
        return false;
    }

    @Override
    public boolean isFolia() {
        return false;
    }

}
