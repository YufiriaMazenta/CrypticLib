package crypticlib.serveradapter;

import crypticlib.scheduler.SpigotScheduler;
import crypticlib.scheduler.BukkitScheduler;
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
    public BukkitScheduler scheduler() {
        return SpigotScheduler.INSTANCE;
    }

    @Override
    public Future<Boolean> teleportEntity(@NotNull Entity entity, @NotNull Location location, PlayerTeleportEvent.@NotNull TeleportCause cause) {
        //Paper 提供了真正的异步传送, 不能退回同步实现: 否则异步调用方会触发线程检查异常,
        //且返回的 Future 语义与 ServerAdapter 声明的不一致
        return entity.teleportAsync(location, cause);
    }

    @Override
    public Future<Boolean> teleportEntity(@NotNull Entity entity, @NotNull Location location) {
        return entity.teleportAsync(location);
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
