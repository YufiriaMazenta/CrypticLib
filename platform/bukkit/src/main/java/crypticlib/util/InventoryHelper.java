package crypticlib.util;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class InventoryHelper {

    /**
     * 将一堆物品放入某个容器中，如果容器放不下则丢在地上
     * @param inventory 要添加物品的容器
     * @param items 要添加的物品
     */
    public static void addItemOrDrop(Inventory inventory, ItemStack... items) {
        HashMap<Integer, ItemStack> failed = inventory.addItem(items);
        Location location = inventory.getLocation();
        if (location == null) {
            //如果一个容器没有坐标，那么其大概率为虚拟容器，此时直接不再处理添加失败的物品
            return;
        }
        failed.forEach((_slot, item) -> {
            location.getWorld().dropItem(location, item);
        });
    }

}
