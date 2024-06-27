package crypticlib.platform;

import crypticlib.scheduler.BukkitScheduler;
import crypticlib.scheduler.IScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Bukkit平台的一些方法集成
 */
public enum BukkitPlatform implements IPlatform {

    INSTANCE;

    @Override
    @NotNull
    public Platform platform() {
        return Platform.BUKKIT;
    }

    @Override
    @NotNull
    public IScheduler scheduler() {
        return BukkitScheduler.INSTANCE;
    }

    @Override
    public void teleportEntity(@NotNull Entity entity, @NotNull Location location, PlayerTeleportEvent.@NotNull TeleportCause cause) {
        entity.teleport(location, cause);
    }

    @Override
    public void teleportEntity(@NotNull Entity entity, @NotNull Location location) {
        teleportEntity(entity, location, PlayerTeleportEvent.TeleportCause.PLUGIN);
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
