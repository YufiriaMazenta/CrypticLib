package crypticlib.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * 物品相关工具类
 */
public class ItemUtil {

    /**
     * 判断物品是否为空
     *
     * @param item 判断的物品
     * @return 返回物品是否有效，当物品为null或者空气时返回true，其余情况返回false
     */
    public static Boolean isAir(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }

}
