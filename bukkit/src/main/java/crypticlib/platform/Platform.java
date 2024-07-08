package crypticlib.platform;

import crypticlib.scheduler.bukkit.IScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

/**
 * 平台接口
 */
public interface Platform {

    /**
     * 获取平台的类型
     *
     * @return 平台的类型
     */
    @NotNull
    Platform.PlatformType type();

    /**
     * 获取平台对应的调度器
     *
     * @return 平台对应的调度器
     */
    @NotNull
    IScheduler scheduler();

    /**
     * 将实体传送至一个坐标
     *
     * @param entity   被传送的实体
     * @param location 传送的目标点
     * @param cause    传送的原因
     */
    void teleportEntity(@NotNull Entity entity, @NotNull Location location, @NotNull PlayerTeleportEvent.TeleportCause cause);

    /**
     * 将实体传送至一个坐标
     *
     * @param entity   被传送的玩家
     * @param location 传送的目标点
     */
    void teleportEntity(@NotNull Entity entity, @NotNull Location location);

    boolean isBukkit();

    boolean isPaper();

    boolean isFolia();

    /**
     * 平台的类型
     */
    enum PlatformType {
        BUKKIT, PAPER, FOLIA;
    }

}
