package crypticlib.platform;

import crypticlib.scheduler.BukkitScheduler;
import crypticlib.scheduler.IScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Bukkit平台的一些方法集成
 */
public enum BukkitPlatform implements IPlatform {

    INSTANCE;

    @Override
    public Platform platform() {
        return Platform.BUKKIT;
    }

    @Override
    public IScheduler scheduler() {
        return BukkitScheduler.INSTANCE;
    }

    @Override
    public void teleportEntity(Entity entity, Location location, PlayerTeleportEvent.TeleportCause cause) {
        entity.teleport(location, cause);
    }

    @Override
    public void teleportEntity(Entity entity, Location location) {
        teleportEntity(entity, location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

}
