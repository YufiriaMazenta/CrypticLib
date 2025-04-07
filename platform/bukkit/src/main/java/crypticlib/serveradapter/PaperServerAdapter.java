package crypticlib.serveradapter;

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
public enum PaperServerAdapter implements ServerAdapter {

    INSTANCE;

    @Override
    @NotNull
    public ServerAdapter.ServerType type() {
        return ServerType.PAPER;
    }

    @Override
    @NotNull
    public Scheduler scheduler() {
        return BukkitScheduler.INSTANCE;
    }

    @Override
    public Future<Boolean> teleportEntity(@NotNull Entity entity, @NotNull Location location, PlayerTeleportEvent.@NotNull TeleportCause cause) {
        return BukkitServerAdapter.INSTANCE.teleportEntity(entity, location, cause);
    }

    @Override
    public Future<Boolean> teleportEntity(@NotNull Entity entity, @NotNull Location location) {
        return BukkitServerAdapter.INSTANCE.teleportEntity(entity, location);
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
