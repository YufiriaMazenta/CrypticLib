package crypticlib.ui.util;

import crypticlib.ui.menu.Menu;
import crypticlib.util.InventoryViewHelper;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MenuHelper {

    /**
     * 获取玩家当前正在打开的菜单
     */
    public static Optional<Menu> getOpeningMenu(Player player) {
        Inventory topInventory = InventoryViewHelper.getTopInventory(player);
        //由于自定义容器没有坐标,所以一旦有坐标就说明不是菜单
        if (topInventory.getLocation() != null)
            return Optional.empty();
        InventoryHolder holder = topInventory.getHolder();
        if (!(holder instanceof Menu))
            return Optional.empty();
        return Optional.of((Menu) holder);
    }

    /**
     * 获取某个容器事件涉及到的菜单
     * 可能为null
     */
    public static Optional<Menu> getMenuHolder(InventoryEvent inventoryEvent) {
        Inventory topInventory = InventoryViewHelper.getTopInventory(inventoryEvent);
        //由于自定义容器没有坐标,所以一旦有坐标就说明不是菜单
        if (topInventory.getLocation() != null)
            return Optional.empty();
        InventoryHolder holder = topInventory.getHolder();
        if (!(holder instanceof Menu))
            return Optional.empty();
        return Optional.of((Menu) holder);
    }

}
