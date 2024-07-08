package crypticlib.platform;

import crypticlib.scheduler.bukkit.BukkitScheduler;
import crypticlib.scheduler.bukkit.IScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Paper平台的一些方法集成
 */
public enum PaperPlatform implements Platform {

    INSTANCE;

    @Override
    @NotNull
    public Platform.PlatformType type() {
        return PlatformType.PAPER;
    }

    @Override
    @NotNull
    public IScheduler scheduler() {
        return BukkitScheduler.INSTANCE;
    }

    @Override
    public void teleportEntity(@NotNull Entity entity, @NotNull Location location, PlayerTeleportEvent.@NotNull TeleportCause cause) {
        BukkitPlatform.INSTANCE.teleportEntity(entity, location, cause);
    }

    @Override
    public void teleportEntity(@NotNull Entity entity, @NotNull Location location) {
        BukkitPlatform.INSTANCE.teleportEntity(entity, location);
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
