package crypticlib.platformadapter;

import crypticlib.scheduler.BukkitScheduler;
import crypticlib.scheduler.Scheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

/**
 * Paper平台的一些方法集成
 */
public enum PaperPlatformAdapter implements PlatformAdapter {

    INSTANCE;

    @Override
    @NotNull
    public PlatformAdapter.PlatformType type() {
        return PlatformType.PAPER;
    }

    @Override
    @NotNull
    public Scheduler scheduler() {
        return BukkitScheduler.INSTANCE;
    }

    @Override
    public Future<Boolean> teleportEntity(@NotNull Entity entity, @NotNull Location location, PlayerTeleportEvent.@NotNull TeleportCause cause) {
        return BukkitPlatformAdapter.INSTANCE.teleportEntity(entity, location, cause);
    }

    @Override
    public Future<Boolean> teleportEntity(@NotNull Entity entity, @NotNull Location location) {
        return BukkitPlatformAdapter.INSTANCE.teleportEntity(entity, location);
    }

    @Override
    public boolean isBukkit() {
        return true;
    }

    @Override
    public boolean isPaper() {
        return true;
    }

    @Override
    public boolean isFolia() {
        return false;
    }

}
