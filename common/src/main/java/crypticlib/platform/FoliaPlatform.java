package crypticlib.platform;

import crypticlib.scheduler.FoliaScheduler;
import crypticlib.scheduler.IScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Folia平台的一些方法集成
 */
public enum FoliaPlatform implements IPlatform {

    INSTANCE;

    @Override
    public Platform platform() {
        return Platform.FOLIA;
    }

    @Override
    public IScheduler scheduler() {
        return FoliaScheduler.INSTANCE;
    }

    @Override
    public void teleportEntity(Entity entity, Location location, PlayerTeleportEvent.TeleportCause cause) {
        entity.teleportAsync(location, cause);
    }

    @Override
    public void teleportEntity(Entity entity, Location location) {
        teleportEntity(entity, location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

}
