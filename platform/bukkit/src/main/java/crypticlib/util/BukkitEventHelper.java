package crypticlib.util;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;

public class BukkitEventHelper {

    /**
     * 是否是玩家移动
     * @param event 移动事件
     * @return 是移动
     */
    public static boolean isMove(PlayerMoveEvent event) {
        Location from = event.getFrom(), to = event.getTo();
        return from.getX() != to.getX() || from.getZ() != to.getZ() || from.getY() != to.getY();
    }

    /**
     * 玩家的方块坐标是否产生变化
     * @param event 移动事件
     * @return 是否产生变化
     */
    public static boolean isBlockMove(PlayerMoveEvent event) {
        Location from = event.getFrom(), to = event.getTo();
        return from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ() || from.getBlockY() != to.getBlockY();
    }

    /**
     * 是否有垂直方向的移动，即Y变化
     * @param event 移动事件
     * @return 是否有垂直移动
     */
    public static boolean isVerticalMove(PlayerMoveEvent event) {
        Location from = event.getFrom(), to = event.getTo();
        return from.getY() != to.getY();
    }

    /**
     * 是否有水平方向的移动，即X和Z变化
     * @param event 移动事件
     * @return 是否有水平移动
     */
    public static boolean isHorizontalMove(PlayerMoveEvent event) {
        Location from = event.getFrom(), to = event.getTo();
        return from.getX() != to.getX() || from.getZ() != to.getZ();
    }

}
