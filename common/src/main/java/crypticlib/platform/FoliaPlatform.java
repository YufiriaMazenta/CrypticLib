package crypticlib.platform;

import crypticlib.scheduler.FoliaScheduler;
import crypticlib.scheduler.IScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Folia平台的一些方法集成
 */
public enum FoliaPlatform implements IPlatform {

    INSTANCE;

    @Override
    @NotNull
    public Platform platform() {
        return Platform.FOLIA;
    }

    @Override
    @NotNull
    public IScheduler scheduler() {
        return FoliaScheduler.INSTANCE;
    }

    @Override
    public void teleportEntity(@NotNull Entity entity, @NotNull Location location, PlayerTeleportEvent.@NotNull TeleportCause cause) {
        entity.teleportAsync(location, cause);
    }

    @Override
    public void teleportEntity(@NotNull Entity entity, @NotNull Location location) {
        teleportEntity(entity, location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

}
