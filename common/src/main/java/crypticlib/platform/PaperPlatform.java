package crypticlib.platform;

import crypticlib.scheduler.BukkitScheduler;
import crypticlib.scheduler.IScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Paper平台的一些方法集成
 */
public enum PaperPlatform implements IPlatform {

    INSTANCE;

    @Override
    public Platform platform() {
        return Platform.PAPER;
    }

    @Override
    public IScheduler scheduler() {
        return BukkitScheduler.INSTANCE;
    }

    @Override
    public void teleportEntity(Entity entity, Location location, PlayerTeleportEvent.TeleportCause cause) {
        BukkitPlatform.INSTANCE.teleportEntity(entity, location, cause);
    }

    @Override
    public void teleportEntity(Entity entity, Location location) {
        BukkitPlatform.INSTANCE.teleportEntity(entity, location);
    }

}
