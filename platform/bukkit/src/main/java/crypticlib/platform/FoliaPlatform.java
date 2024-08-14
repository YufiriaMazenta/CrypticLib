package crypticlib.platform;

import crypticlib.scheduler.FoliaScheduler;
import crypticlib.scheduler.IScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

/**
 * Folia平台的一些方法集成
 */
public enum FoliaPlatform implements Platform {

    INSTANCE;

    @Override
    @NotNull
    public Platform.PlatformType type() {
        return PlatformType.FOLIA;
    }

    @Override
    @NotNull
    public IScheduler scheduler() {
        return FoliaScheduler.INSTANCE;
    }

    @Override
    public Future<Boolean> teleportEntity(@NotNull Entity entity, @NotNull Location location, PlayerTeleportEvent.@NotNull TeleportCause cause) {
        return entity.teleportAsync(location, cause);
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
        return true;
    }

    @Override
    public boolean isFolia() {
        return true;
    }

}
